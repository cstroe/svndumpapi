package com.github.cstroe.svndumpgui.api;

import java.util.Map;

public interface SvnNode {
    Map<SvnNodeHeader, String> getHeaders();
    Map<String, String> getProperties();
    byte[] getContent();

    // utility method
    String get(SvnNodeHeader header);
}
