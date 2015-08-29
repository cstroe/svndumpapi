package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeAdd implements SvnDumpMutator {
    private final int targetRevision;
    private final SvnNode node;

    public NodeAdd(int targetRevision, SvnNode node) {
        this.targetRevision = targetRevision;
        this.node = node;
    }

    @Override
    public void mutate(SvnDump dump) {
        for(SvnRevision revision: dump.getRevisions()) {
            if(revision.getNumber() == targetRevision) {
                revision.getNodes().add(node);
                return;
            }
        }
        throw new IllegalArgumentException("Could not find revision " + targetRevision);
    }
}
