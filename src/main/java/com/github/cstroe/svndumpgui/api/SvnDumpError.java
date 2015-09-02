package com.github.cstroe.svndumpgui.api;

public interface SvnDumpError {
    String getMessage();
    int getRevision();
    SvnNode getNode();
}
