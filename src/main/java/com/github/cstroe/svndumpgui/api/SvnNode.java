package com.github.cstroe.svndumpgui.api;

import java.util.Map;

public interface SvnNode {
    Map<SvnNodeHeader, String> getHeaders();
    void setHeaders(Map<SvnNodeHeader, String> headers);

    Map<String, String> getProperties();
    void setProperties(Map<String, String> properties);

    byte[] getContent();
    void setContent(byte[] content);

    // utility method
    String get(SvnNodeHeader header);
}
