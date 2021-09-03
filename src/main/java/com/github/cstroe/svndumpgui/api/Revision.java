package com.github.cstroe.svndumpgui.api;

import java.util.List;
import java.util.Map;

public interface Revision {
    int getNumber();
    void setNumber(int number);

    Map<String, String> getProperties();
    void setProperties(Map<String, String> properties);

    List<Node> getNodes();
    void setNodes(List<Node> nodes);
    void addNode(Node node);

    /**
     * Utility method to look up the value of a revision property in the {@link #getProperties()} map.
     *
     * @param name the name of a revision property
     * @return the value of the given property
     */
    String get(String name);
}
