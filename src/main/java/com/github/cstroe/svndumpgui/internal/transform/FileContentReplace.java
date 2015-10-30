package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;

import java.util.List;
import java.util.function.Predicate;

public class FileContentReplace extends AbstractRepositoryMutator {
    private final Predicate<Node> nodeMatcher;
    private final List<ContentChunk> contentChunks;

    private boolean skipFileContent = false;

    public FileContentReplace(Predicate<Node> nodeMatcher, List<ContentChunk> contentChunks) {
        this.nodeMatcher = nodeMatcher;
        this.contentChunks = contentChunks;
    }

    @Override
    public void consume(Node node) {
        super.consume(node);
        if(nodeMatcher.test(node)) {
            skipFileContent = true;
            boolean hasChunks = false;
            for(ContentChunk chunk : contentChunks) {
                hasChunks = true;
                super.consume(chunk);
            }
            if(hasChunks) {
                super.endChunks();
            }
        }
    }

    @Override
    public void consume(ContentChunk chunk) {
        if(!skipFileContent) {
            super.consume(chunk);
        }
    }

    @Override
    public void endChunks() {
        if(!skipFileContent) {
            super.endChunks();
        }
    }

    @Override
    public void endNode(Node node) {
        super.endNode(node);
        skipFileContent = false;
    }
}
