package com.github.cstroe.svndumpgui.api;

import java.util.List;
import java.util.Map;

public interface SvnRevision extends SvnProperty {
    int getNumber();
    Map<String, String> getProperties();
    List<SvnNode> getNodes();

    // utility method
    String get(String name);
}
