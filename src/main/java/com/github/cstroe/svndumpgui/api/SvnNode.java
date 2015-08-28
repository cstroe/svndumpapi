package com.github.cstroe.svndumpgui.api;

import java.util.Map;

public interface SvnNode {
    String getPath();
    String getKind();
    String getAction();
    String getMd5();
    String getSha1();
    Integer getCopiedFromRevision();
    String getCopiedFromPath();
    String getCopiedFromMd5();
    String getCopiedFromSha1();
    byte[] getContent();
    Map<String, String> getProperties();
}
