package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class ClearRevision extends AbstractSvnDumpMutator {

    private final int NOT_SET = -1;
    private final int fromRevision;
    private final int toRevision;

    private boolean matchedSomething = false;
    private boolean inClearedRevision = false;

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

    private boolean revisionMatches(SvnRevision revision) {
        return (toRevision == NOT_SET && revision.getNumber() == fromRevision) ||
               (revision.getNumber() >= fromRevision && revision.getNumber() <= toRevision);
    }

    @Override
    public void consume(SvnRevision revision) {
        if(revisionMatches(revision)) {
            inClearedRevision = true;
            matchedSomething = true;
        }
        super.consume(revision);
    }

    @Override
    public void consume(SvnNode node) {
        if(!inClearedRevision) {
            super.consume(node);
        }
    }

    @Override
    public void consume(FileContentChunk chunk) {
        if(!inClearedRevision) {
            super.consume(chunk);
        }
    }

    @Override
    public void endNode(SvnNode node) {
        if(!inClearedRevision) {
            super.endNode(node);
        }
    }

    @Override
    public void endChunks() {
        if(!inClearedRevision) {
            super.endChunks();
        }
    }

    @Override
    public void endRevision(SvnRevision revision) {
        inClearedRevision = false;
        super.endRevision(revision);
    }

    @Override
    public void finish() {
        if(!matchedSomething) {
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
