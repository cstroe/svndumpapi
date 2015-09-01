package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator extends SvnDumpConsumer {
    boolean isValid(SvnDump dump);
    SvnDumpError getError();
}
