package com.github.cstroe.svndumpgui.internal.transform.replace;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.TreeOfKnowledge;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.consumer.ImmutableTreeOfKnowledge;
import com.github.cstroe.svndumpgui.internal.consumer.TreeOfKnowledgeImpl;
import com.github.cstroe.svndumpgui.internal.transform.AbstractRepositoryMutator;
import com.github.cstroe.svndumpgui.internal.utility.Md5;
import com.github.cstroe.svndumpgui.internal.utility.Sha1;

import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.cstroe.svndumpgui.internal.utility.Preconditions.checkNotNull;

public class FileContentReplace extends AbstractRepositoryMutator {
    private final Predicate<Node> nodeMatcher;
    private final Function<Node, ContentChunk> contentChunkGenerator;

    private boolean nodeMatched = false;
    private ContentChunk generatedChunk = null;

    private final TreeOfKnowledge tok;

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
        this.tok = new TreeOfKnowledgeImpl();
    }

    protected TreeOfKnowledge getTreeOfKnowledge() {
        return new ImmutableTreeOfKnowledge(tok);
    }

    @Override
    public void consume(Node node) {
        tok.consume(node);
        if("file".equals(node.get(NodeHeader.KIND))) {
            if(nodeMatcher.test(node)) {
                nodeMatched = true;
                generatedChunk = checkNotNull(contentChunkGenerator.apply(node));
                return; // we're outta here
            } else {
                // node was not matched, but it might be a copy of a previously replaced node
                Node previousNode;
                String copyFromRev = node.get(NodeHeader.COPY_FROM_REV);
                if (copyFromRev != null) {
                    int copyRevision = Integer.parseInt(copyFromRev);
                    String copyPath = node.get(NodeHeader.COPY_FROM_PATH);

                    previousNode = tok.tellMeAbout(copyRevision, copyPath);

                    if (previousNode != null) {
                        // node was previously matched
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
                    }
                }
            }
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
            updateHeaders(node);

            node.getContent().clear();
            node.addFileContentChunk(generatedChunk);

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
