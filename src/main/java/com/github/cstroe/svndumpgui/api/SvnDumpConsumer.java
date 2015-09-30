package com.github.cstroe.svndumpgui.api;

public interface SvnDumpConsumer {
    void consume(SvnDumpPreamble preamble);
    void consume(SvnRevision revision);
    void consume(SvnNode node);
    void finish();
}
