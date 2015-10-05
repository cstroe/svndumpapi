package com.github.cstroe.svndumpgui.api;

public interface SvnDumpValidationError {
    String getMessage();
    int getRevision();
    SvnNode getNode();
}
