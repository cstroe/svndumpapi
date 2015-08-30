package com.github.cstroe.svndumpgui.api;

public interface SvnDumpError {
    String getMessage();
    SvnRevision getRevision();
    SvnNode getNode();
}
