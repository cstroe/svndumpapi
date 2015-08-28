package com.github.cstroe.svndumpgui.api;

import java.util.List;
import java.util.Map;

public interface SvnRevision extends SvnProperties {
    int getNumber();
    String getProperty(String name);
    Map<String, String> getProperties();
    List<SvnNode> getNodes();
}
