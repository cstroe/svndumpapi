package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator extends SvnDumpConsumer {
    boolean isValid();
    SvnDumpValidationError getError();
}
