package com.github.cstroe.svndumpgui.api;

public interface SvnNode {
    String getPath();
    String getKind();
    String getAction();
    String getMd5();
    String getSha1();
}
