package com.github.cstroe.svndumpgui.api;

import java.util.Iterator;

public interface SvnDumpMutator extends SvnDumpConsumer {
    default void mutate(SvnDump dump) {
        consume(dump.getPreamble());
        Iterator<SvnRevision> revisionsIter = dump.revisions();
        while(revisionsIter.hasNext()) {
            consume(revisionsIter.next());
        }
        finish();
    }
}
