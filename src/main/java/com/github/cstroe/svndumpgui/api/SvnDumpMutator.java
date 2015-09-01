package com.github.cstroe.svndumpgui.api;

import java.util.Iterator;

public interface SvnDumpMutator {
    void mutateRevision(SvnRevision revision);
    void finish();

    default void mutate(SvnDump dump) {
        Iterator<SvnRevision> revisionsIter = dump.revisions();
        while(revisionsIter.hasNext()) {
            mutateRevision(revisionsIter.next());
        }
        finish();
    }
}
