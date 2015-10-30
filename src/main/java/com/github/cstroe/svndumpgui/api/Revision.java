package com.github.cstroe.svndumpgui.api;

import java.util.List;
import java.util.Map;

public interface Revision extends Property {
    int getNumber();
    void setNumber(int number);

    Map<String, String> getProperties();
    void setProperties(Map<String, String> properties);

    List<Node> getNodes();
    void setNodes(List<Node> nodes);
    void addNode(Node node);

    // utility method
    String get(String name);
}
