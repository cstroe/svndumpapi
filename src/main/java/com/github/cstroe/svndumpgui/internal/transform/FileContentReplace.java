package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNode;

import java.util.List;
import java.util.function.Predicate;

public class FileContentReplace extends AbstractSvnDumpMutator {
    private final Predicate<SvnNode> nodeMatcher;
    private final List<FileContentChunk> contentChunks;

    private boolean skipFileContent = false;

    public FileContentReplace(Predicate<SvnNode> nodeMatcher, List<FileContentChunk> contentChunks) {
        this.nodeMatcher = nodeMatcher;
        this.contentChunks = contentChunks;
    }

    @Override
    public void consume(SvnNode node) {
        super.consume(node);
        if(nodeMatcher.test(node)) {
            skipFileContent = true;
            boolean hasChunks = false;
            for(FileContentChunk chunk : contentChunks) {
                hasChunks = true;
                super.consume(chunk);
            }
            if(hasChunks) {
                super.endChunks();
            }
        }
    }

    @Override
    public void consume(FileContentChunk chunk) {
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
    public void endNode(SvnNode node) {
        super.endNode(node);
        skipFileContent = false;
    }
}
