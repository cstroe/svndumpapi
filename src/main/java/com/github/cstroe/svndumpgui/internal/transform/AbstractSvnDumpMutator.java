package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.Iterator;

public abstract class AbstractSvnDumpMutator implements SvnDumpMutator {
    @Override
    public void mutate(SvnDump dump) {
        Iterator<SvnRevision> revisionsIter = dump.revisions();
        while(revisionsIter.hasNext()) {
            mutate(revisionsIter.next());
        }
        finish();
    }
}
