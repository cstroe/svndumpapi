package com.github.cstroe.svndumpgui.api;

public interface SvnRevision extends SvnProperties {
    int getNumber();
    String getProperty(String name);
}
