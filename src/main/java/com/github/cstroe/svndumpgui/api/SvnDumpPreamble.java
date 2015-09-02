package com.github.cstroe.svndumpgui.api;

public interface SvnDumpPreamble extends SvnVisitable{
    String getUUID();
    void setUUID(String uuid);
}
