package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class ClearRevision extends AbstractSvnDumpMutator {

    private final int NOT_SET = -1;
    private final int fromRevision;
    private final int toRevision;

    private boolean changedSomething = false;

    public ClearRevision(int revision) {
        if(revision < 0 ) {
            throw new IllegalArgumentException("Negative revision numbers not allowed");
        }
        this.fromRevision = revision;
        this.toRevision = NOT_SET;
    }

    public ClearRevision(int fromRevision, int toRevision) {
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
    public void consume(SvnNode node) {
        final SvnRevision revision = node.getRevision().get();
        if(toRevision == NOT_SET && revision.getNumber() == fromRevision) {
            changedSomething = true;
            return;
        } else if(revision.getNumber() >= fromRevision && revision.getNumber() <= toRevision) {
            changedSomething = true;
            return;
        } else {
            super.consume(node);
        }
    }

    @Override
    public void finish() {
        if(!changedSomething) {
            throw new IllegalArgumentException("No revisions matched: " + toString());
        }
        super.finish();
    }

    @Override
    public String toString() {
        return "NodeClear{" +
                "fromRevision=" + fromRevision +
                ", toRevision=" + toRevision +
                '}';
    }
}
