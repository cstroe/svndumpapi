package com.github.cstroe.svndumpgui.api;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public interface SvnRevision extends SvnProperty {
    int getNumber();
    void setNumber(int number);

    Map<String, String> getProperties();
    void setProperties(Map<String, String> properties);

    List<SvnNode> getNodes();
    void setNodes(List<SvnNode> nodes);

    Iterator<SvnNode> nodes();

    // utility method
    String get(String name);
}
