package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator extends SvnDumpConsumer {
    boolean isValid();
    SvnDumpError getError();

    default boolean validate(SvnDump dump) {
        consume(dump.getPreamble());
        for(SvnRevision revision : dump.getRevisions()) {
            consume(revision);
            for(SvnNode node : revision.getNodes()) {
                consume(node);
            }
        }
        return isValid();
    }
}
