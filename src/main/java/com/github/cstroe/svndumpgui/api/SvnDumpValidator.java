package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator extends SvnDumpConsumer {
    void consumePreamble(SvnDump dump);
    void consumeRevision(SvnRevision revision);
    boolean isValid();
    SvnDumpError getError();

    default boolean validate(SvnDump dump) {
        consumePreamble(dump);
        for(SvnRevision revision : dump.getRevisions()) {
            consumeRevision(revision);
        }
        return isValid();
    }
}
