package com.github.cstroe.svndumpgui.api;

import java.util.Iterator;

public interface SvnDumpMutator extends SvnDumpConsumer {
    default void mutate(SvnDump dump) {
        Iterator<SvnRevision> revisionsIter = dump.revisions();
        while(revisionsIter.hasNext()) {
            consumeRevision(revisionsIter.next());
        }
        finish();
    }
}
