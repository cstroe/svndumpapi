package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator extends SvnDumpConsumer {
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
