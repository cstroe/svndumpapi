package com.github.cstroe.svndumpgui.api;

public interface SvnDumpConsumer {
    void consumePreamble(SvnDump dump);
    void consumeRevision(SvnRevision revision);
    void finish();
}
