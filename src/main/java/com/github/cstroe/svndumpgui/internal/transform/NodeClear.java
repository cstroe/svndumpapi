package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeClear implements SvnDumpMutator {

    private final int NOT_SET = -1;
    private final int fromRevision;
    private final int toRevision;

    public NodeClear(int revision) {
        if(revision < 0 ) {
            throw new IllegalArgumentException("Negative revision numbers not allowed");
        }
        this.fromRevision = revision;
        this.toRevision = NOT_SET;
    }

    public NodeClear(int fromRevision, int toRevision) {
        if(fromRevision < 0 || toRevision < 0) {
            throw new IllegalArgumentException("Negative revision numbers not allowed");
        }
        if(fromRevision >= toRevision) {
            throw new IllegalArgumentException("Range must span at least 2 revisions in ascending order.");
        }
        this.fromRevision = fromRevision;
        this.toRevision = toRevision;
    }

    @Override
    public void mutate(SvnDump dump) {
        for(SvnRevision revision : dump.getRevisions()) {
            if(toRevision == NOT_SET && revision.getNumber() == fromRevision) {
                revision.getNodes().clear();
            } else if(revision.getNumber() >= fromRevision && revision.getNumber() <= toRevision) {
                revision.getNodes().clear();
            }
        }
    }
}
