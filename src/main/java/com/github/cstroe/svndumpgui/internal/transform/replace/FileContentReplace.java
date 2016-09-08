package com.github.cstroe.svndumpgui.internal.transform.replace;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.transform.AbstractRepositoryMutator;
import com.github.cstroe.svndumpgui.internal.utility.Md5;
import com.github.cstroe.svndumpgui.internal.utility.Sha1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.cstroe.svndumpgui.internal.utility.Preconditions.checkNotNull;

public class FileContentReplace extends AbstractRepositoryMutator {
    private final Predicate<Node> nodeMatcher;
    private final Function<Node, ContentChunk> contentChunkGenerator;

    private boolean nodeMatched = false;

    private Map<Integer, Set<Node>> previouslyUpdated = new HashMap<>();
    private int currentRevision;

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
    public static FileContentReplace createFCR(int revision,
                                               String action,
                                               String path,
                                               Function<Node, ContentChunk> chunkGenerator) {
        return new FileContentReplace(
                nodeMatch(revision, action, path),
                chunkGenerator
        );
    }

    public FileContentReplace(Predicate<Node> nodeMatcher, Function<Node, ContentChunk> contentChunkGenerator) {
        this.nodeMatcher = checkNotNull(nodeMatcher);
        this.contentChunkGenerator = checkNotNull(contentChunkGenerator);
    }

    @Override
    public void consume(Revision revision) {
        currentRevision = revision.getNumber();
        super.consume(revision);
    }

    @Override
    public void consume(Node node) {
        final String nodeAction = node.get(NodeHeader.ACTION);
        if("delete".equals(nodeAction)) {
            // we don't care about deletes
            super.consume(node);
            return;
        }

        final String nodeKind = node.get(NodeHeader.KIND);
        if("file".equals(nodeKind) && nodeMatcher.test(node)) {
            // we found a match for replacement
            nodeMatched = true;
            return;
        }

        // node was not matched, but it might be a copy of a previously replaced node
        String copyFromRev = node.get(NodeHeader.COPY_FROM_REV);
        if(copyFromRev == null) {
            // nope, it's not a copy
            super.consume(node);
            return;
        }

        int copyRevision = Integer.parseInt(copyFromRev);
        String copyPath = node.get(NodeHeader.COPY_FROM_PATH);

        Node previousNode = findPreviouslyUpdatedNode(copyRevision, copyPath);
        if (previousNode != null) {
            // node was previously matched
            recordNodeUpdate(currentRevision, node);

            String previousMd5 = previousNode.get(NodeHeader.MD5);
            String previousSha1 = previousNode.get(NodeHeader.SHA1);
            String previousCopyMd5 = previousNode.get(NodeHeader.SOURCE_MD5);
            String previousCopySha1 = previousNode.get(NodeHeader.SOURCE_SHA1);
            String currentSourceMd5 = node.get(NodeHeader.SOURCE_MD5);
            String currentSourceSha1 = node.get(NodeHeader.SOURCE_SHA1);

            String md5 = previousMd5;
            if (md5 == null) {
                md5 = previousCopyMd5;
            }

            if (!md5.equals(currentSourceMd5)) {
                node.getHeaders().put(NodeHeader.SOURCE_MD5, md5);
            }

            String sha1 = previousSha1;
            if (sha1 == null) {
                sha1 = previousCopySha1;
            }

            if (!sha1.equals(currentSourceSha1)) {
                node.getHeaders().put(NodeHeader.SOURCE_SHA1, sha1);
            }
            super.consume(node);
            return;
        }

        if("dir".equals(nodeKind) && "add".equals(nodeAction)) {
            // we might be moving the directory that contains a previously replaced node
            Node previouslyContainedNode = findPreviouslyUpdatedNodeFromDirectory(copyRevision, copyPath);
            if(previouslyContainedNode != null) {
                // yes, we're moving the directory that contains this node
                String newDirectoryPath = node.get(NodeHeader.PATH);
                Node nodeCopy = new NodeImpl(previouslyContainedNode);
                String nodePath = nodeCopy.get(NodeHeader.PATH);
                String newPath = newDirectoryPath + nodePath.substring(copyPath.length());
                nodeCopy.getHeaders().put(NodeHeader.PATH, newPath);
                recordNodeUpdate(currentRevision, nodeCopy);
            }
            super.consume(node);
            return;
        }

        super.consume(node);
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
            ContentChunk generatedChunk = checkNotNull(contentChunkGenerator.apply(node));
            updateHeaders(node, generatedChunk);

            node.getContent().clear();
            node.addFileContentChunk(generatedChunk);

            continueNodeConsumption(node);
            recordNodeUpdate(currentRevision, node);
            nodeMatched = false;
        }
    }

    private void recordNodeUpdate(int currentRevision, Node node) {
        if(!previouslyUpdated.containsKey(currentRevision)) {
            previouslyUpdated.put(currentRevision, new HashSet<>());
        }
        previouslyUpdated.get(currentRevision).add(node);
    }

    private Node findPreviouslyUpdatedNode(int copyRevision, String copyPath) {
        Set<Node> nodeSet = previouslyUpdated.get(copyRevision);
        if(nodeSet != null) {
            for(Node previouslyReplacedNode : nodeSet) {
                if(copyPath.equals(previouslyReplacedNode.get(NodeHeader.PATH))) {
                    return previouslyReplacedNode;
                }
            }
        }
        return null;
    }

    private Node findPreviouslyUpdatedNodeFromDirectory(int copyRevision, String directoryPath) {
        Set<Node> nodeSet = previouslyUpdated.get(copyRevision);
        if(nodeSet != null) {
            for(Node previouslyReplacedNode : nodeSet) {
                if(previouslyReplacedNode.get(NodeHeader.PATH).startsWith(directoryPath)) {
                    return previouslyReplacedNode;
                }
            }
        }
        return null;
    }

    private void updateHeaders(Node node, ContentChunk generatedChunk) {
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

    private void continueNodeConsumption(Node node) {
        final String trailingNewLine = node.getProperties().get(Property.TRAILING_NEWLINE_HINT);
        node.getProperties().remove(Property.TRAILING_NEWLINE_HINT);

        super.consume(node);
        for(ContentChunk generatedChunk : node.getContent()) {
            super.consume(generatedChunk);
        }
        super.endChunks();

        if(trailingNewLine != null) {
            node.getProperties().put(Property.TRAILING_NEWLINE_HINT, trailingNewLine);
        }

        super.endNode(node);
    }
}
