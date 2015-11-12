package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;

import java.util.function.Function;
import java.util.function.Predicate;

public class FileContentReplace extends AbstractRepositoryMutator {
    private final Predicate<Node> nodeMatcher;
    private final Function<Node, ContentChunk> contentChunkGenerator;

    private boolean skipFileContent = false;

    public FileContentReplace(Predicate<Node> nodeMatcher, Function<Node, ContentChunk> contentChunkGenerator) {
        this.nodeMatcher = nodeMatcher;
        this.contentChunkGenerator = contentChunkGenerator;
    }

    @Override
    public void consume(Node node) {
        super.consume(node);
        if(nodeMatcher.test(node)) {
            skipFileContent = true;
            ContentChunk chunk = contentChunkGenerator.apply(node);
            if(chunk != null) {
                super.consume(chunk);
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
