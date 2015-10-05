package com.github.cstroe.svndumpgui.api;

import java.util.Iterator;

public interface SvnDumpMutator extends SvnDumpConsumer {
    default void mutate(SvnDump dump) {
        // TODO: Remove this class!
    }
}
