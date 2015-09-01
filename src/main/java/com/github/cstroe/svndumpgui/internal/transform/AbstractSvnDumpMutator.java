package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.Iterator;

public abstract class AbstractSvnDumpMutator implements SvnDumpMutator {
    @Override
    public void mutate(SvnDump dump) {
        Iterator<SvnRevision> revisionsIter = dump.revisions();
        while(revisionsIter.hasNext()) {
            SvnRevision currentRevision = revisionsIter.next();

            mutate(currentRevision);

            Iterator<SvnNode> nodesIter = currentRevision.nodes();
            while(nodesIter.hasNext()) {
                mutate(nodesIter.next());
            }
        }
        finish();
    }

    @Override
    public void mutate(SvnRevision revision) {}

    @Override
    public void mutate(SvnNode node) {}

    @Override
    public void finish() {}
}
