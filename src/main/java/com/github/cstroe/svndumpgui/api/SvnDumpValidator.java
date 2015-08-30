package com.github.cstroe.svndumpgui.api;


public interface SvnDumpValidator {
    boolean isValid(SvnDump dump);
    SvnDumpError getError();
}
