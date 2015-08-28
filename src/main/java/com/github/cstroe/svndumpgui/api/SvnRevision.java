package com.github.cstroe.svndumpgui.api;

import java.util.List;
import java.util.Set;

public interface SvnRevision extends SvnProperties {
    int getNumber();
    String getProperty(String name);
    Set<String> getDefinedProperties();
    List<SvnNode> getNodes();
}
