package com.github.cstroe.svndumpgui.internal.transform;

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
    public void mutate(SvnRevision revision) {
        if(revision.getNumber() == targetRevision) {
            revision.getNodes().add(node);
        }
    }

    @Override
    public void finish() {
        if(!nodeAdded) {
            throw new IllegalArgumentException("Could not find revision " + targetRevision);
        }
        nodeAdded = false;
    }
}
