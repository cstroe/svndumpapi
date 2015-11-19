package com.github.cstroe.svndumpgui.internal.transform.replace;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.transform.AbstractRepositoryMutator;
import com.github.cstroe.svndumpgui.internal.utility.FileOperations;
import com.github.cstroe.svndumpgui.internal.utility.Md5;
import com.github.cstroe.svndumpgui.internal.utility.Sha1;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.cstroe.svndumpgui.internal.utility.Preconditions.checkNotNull;

public class FileContentReplace extends AbstractRepositoryMutator {
    private final Predicate<Node> nodeMatcher;
    private final Function<Node, ContentChunk> contentChunkGenerator;

    private boolean nodeMatched = false;
    private ContentChunk generatedChunk = null;

    private FileSystem fs;
    private static final String NODE_ATTR = "user:node";

    /**
     * Helper method to generate a predicate that matches a node,
     * given the revision number, node action, and node path.
     */
    public static Predicate<Node> nodeMatch(int revision, String action, String path) {
        return n ->
            n.getRevision().get().getNumber() == revision &&
            action.equals(n.get(NodeHeader.ACTION)) &&
            path.equals(n.get(NodeHeader.PATH));
    }

    /**
     * Helper method to generate a {@link ContentChunk} from a String.
     */
    public static Function<Node, ContentChunk> chunkFromString(String content) {
        return n -> new ContentChunkImpl(content.getBytes());
    }

    /**
     * Helper function for creating a FileContentReplace using a
     * {@link #nodeMatch(int, String, String) node matcher} and a {@link #chunkFromString(String) string}.
     */
    public static FileContentReplace createFCR(int revision, String action, String path, Function<Node, ContentChunk> chunkGenerator) {
        return new FileContentReplace(
                nodeMatch(revision, action, path),
                chunkGenerator
        );
    }

    public FileContentReplace(Predicate<Node> nodeMatcher, Function<Node, ContentChunk> contentChunkGenerator) {
        this.nodeMatcher = checkNotNull(nodeMatcher);
        this.contentChunkGenerator = checkNotNull(contentChunkGenerator);
        this.fs = Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("basic", "user").build());
    }

    @Override
    public void consume(Node node) {
        final String theKindOfNodeWeHave = node.get(NodeHeader.KIND);
        final String theNodeAction = node.get(NodeHeader.ACTION);
        final String copyFromPath = node.get(NodeHeader.COPY_FROM_PATH);

        if("delete".equals(theNodeAction)) {
            deletePath(node);
        } else if("file".equals(theKindOfNodeWeHave)) {
            if(nodeMatcher.test(node)) {
                nodeMatched = true;
                generatedChunk = checkNotNull(contentChunkGenerator.apply(node));
                return; // we're outta here
            }

            if("add".equals(theNodeAction) && copyFromPath != null) {
                Path previouslyMatchedPath = fs.getPath("/" + copyFromPath);
                if(Files.exists(previouslyMatchedPath)) {
                    try {
                        Node changedNode = NodeSerializer.fromBytes((byte[])Files.getAttribute(previouslyMatchedPath, NODE_ATTR));

                        node.getHeaders().put(NodeHeader.SOURCE_MD5, changedNode.get(NodeHeader.MD5));
                        node.getHeaders().put(NodeHeader.SOURCE_SHA1, changedNode.get(NodeHeader.SHA1));

                        Path newPath = fs.getPath("/" + node.get(NodeHeader.PATH));
                        Path parentDir = newPath.getParent();
                        if(!Files.exists(parentDir)) {
                            Files.createDirectory(parentDir);
                        }
                        Files.createFile(newPath);

                        Files.setAttribute(newPath, NODE_ATTR, NodeSerializer.toBytes(changedNode));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } else if("dir".equals(theKindOfNodeWeHave) && "add".equals(theNodeAction) && copyFromPath != null) {
            Path previouslyMatchedPath = fs.getPath("/" + copyFromPath);
            if (Files.exists(previouslyMatchedPath)) {
                Path newPath = fs.getPath("/" + node.get(NodeHeader.PATH));
                try {
                    FileOperations.RecursiveCopier tc = new FileOperations.RecursiveCopier(previouslyMatchedPath, newPath);
                    Files.walkFileTree(previouslyMatchedPath, tc);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.consume(node);
    }

    private void deletePath(Node node) {
        Path currentFile = fs.getPath("/" + node.get(NodeHeader.PATH));
        if(Files.exists(currentFile)) {
            try {
                if(Files.isDirectory(currentFile)) {
                    Files.walkFileTree(currentFile, new FileOperations.RecursiveDeleter());
                } else {
                    Files.deleteIfExists(currentFile);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void consume(ContentChunk chunk) {
        if(!nodeMatched) {
            super.consume(chunk);
        }
    }

    @Override
    public void endChunks() {
        if(!nodeMatched) {
            super.endChunks();
        }
    }

    @Override
    public void endNode(Node node) {
        if(!nodeMatched) {
            super.endNode(node);
        } else {
            updateHeaders(node);

            node.getContent().clear();
            node.addFileContentChunk(generatedChunk);

            rememberNode(node);
            continueNodeConsumption(node);
        }
        nodeMatched = false;
        generatedChunk = null;
    }

    private void updateHeaders(Node node) {
        node.getHeaders().put(NodeHeader.TEXT_CONTENT_LENGTH, Integer.toString(generatedChunk.getContent().length));

        String propContentLengthRaw = node.get(NodeHeader.PROP_CONTENT_LENGTH);
        long propContentLength = 0;
        if(propContentLengthRaw != null) {
            propContentLength = Long.parseLong(propContentLengthRaw);
        }

        node.getHeaders().put(NodeHeader.CONTENT_LENGTH, Long.toString(propContentLength + generatedChunk.getContent().length));

        if (node.get(NodeHeader.MD5) != null) {
            final String md5hash = new Md5().hash(generatedChunk.getContent());
            node.getHeaders().put(NodeHeader.MD5, md5hash);
        }

        if (node.get(NodeHeader.SHA1) != null) {
            final String sha1hash = new Sha1().hash(generatedChunk.getContent());
            node.getHeaders().put(NodeHeader.SHA1, sha1hash);
        }
    }

    private void rememberNode(Node node) {
        Path matchedPath = fs.getPath("/" + node.get(NodeHeader.PATH));
        try {
            Files.createFile(matchedPath);
            Files.setAttribute(matchedPath, NODE_ATTR, NodeSerializer.toBytes(node));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void continueNodeConsumption(Node node) {
        final String trailingNewLine = node.getProperties().get(Property.TRAILING_NEWLINE_HINT);
        node.getProperties().remove(Property.TRAILING_NEWLINE_HINT);

        super.consume(node);
        super.consume(generatedChunk);
        super.endChunks();

        if(trailingNewLine != null) {
            node.getProperties().put(Property.TRAILING_NEWLINE_HINT, trailingNewLine);
        }

        super.endNode(node);
    }
}
