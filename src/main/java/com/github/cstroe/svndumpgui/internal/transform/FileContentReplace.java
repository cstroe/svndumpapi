package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.internal.utility.Md5;
import com.github.cstroe.svndumpgui.internal.utility.Sha1;

import java.util.function.Function;
import java.util.function.Predicate;

public class FileContentReplace extends AbstractRepositoryMutator {
    private final Predicate<Node> nodeMatcher;
    private final Function<Node, ContentChunk> contentChunkGenerator;

    private boolean nodeMatched = false;
    private ContentChunk generatedChunk = null;

    public FileContentReplace(Predicate<Node> nodeMatcher, Function<Node, ContentChunk> contentChunkGenerator) {
        this.nodeMatcher = nodeMatcher;
        this.contentChunkGenerator = contentChunkGenerator;
    }

    @Override
    public void consume(Node node) {
        if(nodeMatcher.test(node)) {
            nodeMatched = true;
            generatedChunk = contentChunkGenerator.apply(node);
            if(generatedChunk == null) {
                throw new NullPointerException("Cannot provide null node content.");
            }
        } else {
            super.consume(node);
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

            node.getContent().clear();
            node.addFileContentChunk(generatedChunk);

            super.consume(node);
            super.consume(generatedChunk);
            super.endChunks();
            super.endNode(node);
        }
        nodeMatched = false;
        generatedChunk = null;
    }
}
