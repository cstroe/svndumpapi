package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Revision;

public class NodeAdd extends AbstractRepositoryMutator {
    private final int targetRevision;
    private final Node node;

    private boolean nodeAdded = false;

    public NodeAdd(int targetRevision, Node node) {
        this.targetRevision = targetRevision;
        this.node = node;
    }

    @Override
    public void consume(Revision revision) {
        super.consume(revision);
        if(revision.getNumber() == targetRevision) {
            node.setRevision(revision);
            super.consume(node);
            if(node.getContent().size() > 0) {
                for (ContentChunk chunk : node.getContent()) {
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
