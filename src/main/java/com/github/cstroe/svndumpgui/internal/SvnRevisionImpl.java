package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SvnRevisionImpl implements SvnRevision {

    private int number;
    private Map<String, String> properties = new LinkedHashMap<>();
    private List<SvnNode> nodes = new ArrayList<>();

    public SvnRevisionImpl(int number) {
        this(number, null);
    }

    public SvnRevisionImpl(int number, String date) {
        this.number = number;
        if(date != null) {
            this.properties.put(DATE, date);
        }
    }

    public SvnRevisionImpl(SvnRevision revision) {
        this.number = revision.getNumber();
        this.properties = new LinkedHashMap<>(revision.getProperties());
        for(SvnNode node : revision.getNodes()) {
            nodes.add(new SvnNodeImpl(node));
        }
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public void setNumber(int number) {
        this.number = number;
    }

    @Override
    public String get(String name) {
        return properties.get(name);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
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

    @Override
    public void addNode(SvnNode node) {
        if(node == null) {
            throw new NullPointerException("Cannot add null node to SvnRevision.");
        }
        nodes.add(node);
    }

    @Override
    public void setNodes(List<SvnNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        String date = get(SvnProperty.DATE);
        String author = get(SvnProperty.AUTHOR);
        String log = get(SvnProperty.LOG);

        StringBuilder builder = new StringBuilder();
        builder.append("Revision: ")
               .append(number)
               .append(", ");

        if(log != null) {
            builder.append(log);
        } else {
            builder.append("*** empty message ***");
        }

        if(author != null || date != null) {
            builder.append(" - ");
        }

        if(author != null) {
            builder.append(author);
        }

        if(date != null) {
            if(author != null) {
                builder.append(" @ ");
            }
            builder.append(date);
        }

        return builder.toString();
    }
}
