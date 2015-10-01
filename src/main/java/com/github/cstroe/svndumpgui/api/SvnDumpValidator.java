package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator extends SvnDumpConsumer {
    boolean isValid();
    SvnDumpValidationError getError();

    default boolean validate(SvnDump dump) {
        consume(dump.getPreamble());
        for(SvnRevision revision : dump.getRevisions()) {
            if(!isValid()) {
                return false;
            }
            consume(revision);
            for(SvnNode node : revision.getNodes()) {
                if(!isValid()) {
                    return false;
                }
                consume(node);
            }
        }
        finish();
        return isValid();
    }
}
