package com.github.cstroe.svndumpgui.api;

import java.util.List;

public interface SvnRevision extends SvnProperties {
    int getNumber();
    String getProperty(String name);
    List<SvnNode> getNodes();
}
