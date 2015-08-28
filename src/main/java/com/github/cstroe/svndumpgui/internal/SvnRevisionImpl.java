package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SvnRevisionImpl implements SvnRevision {

    private final int number;
    private Map<String, String> properties = new LinkedHashMap<>();
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
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        if(properties == null) {
            throw new NullPointerException("Cannot set null properties on SvnRevision.");
        }
        this.properties = new LinkedHashMap<>(properties);
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
