package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.*;

public class SvnRevisionImpl implements SvnRevision {

    private final int number;
    private final Map<String, String> properties = new HashMap<>();
    private final List<SvnNode> nodes = new ArrayList<>();

    public SvnRevisionImpl(int number) {
        this(number, null);
    }

    public SvnRevisionImpl(int number, String date) {
        this.number = number;
        this.properties.put(DATE, date);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    @Override
    public Set<String> getDefinedProperties() {
        return properties.keySet();
    }

    public void setProperties(Map<String, String> properties) {
        if(properties == null) {
            throw new NullPointerException("Cannot set null properties on SvnRevision.");
        }
        this.properties.putAll(properties);
    }

    @Override
    public List<SvnNode> getNodes() {
        return nodes;
    }

    public void addNode(SvnNode node) {
        if(node == null) {
            throw new NullPointerException("Cannot add null node to SvnRevision.");
        }
        nodes.add(node);
    }
}
