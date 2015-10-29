package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeAdd extends AbstractSvnDumpMutator {
    private final int targetRevision;
    private final SvnNode node;

    private boolean nodeAdded = false;

    public NodeAdd(int targetRevision, SvnNode node) {
        this.targetRevision = targetRevision;
        this.node = node;
    }

    @Override
    public void consume(SvnRevision revision) {
        super.consume(revision);
        if(revision.getNumber() == targetRevision && !nodeAdded) {
            node.setRevision(revision);
            super.consume(node);
            if(node.getContent().size() > 0) {
                for (FileContentChunk chunk : node.getContent()) {
                    super.consume(chunk);
                }
                super.endChunks();
            }
            super.endNode(node);
            nodeAdded = true;
        }
    }

    @Override
    public void finish() {
        if(!nodeAdded) {
            throw new IllegalArgumentException("Could not find revision " + targetRevision);
        }
        nodeAdded = false;
        super.finish();
    }
}
