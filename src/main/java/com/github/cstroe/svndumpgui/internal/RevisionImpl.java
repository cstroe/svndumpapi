package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RevisionImpl implements Revision {

    private int number;
    private Map<String, String> properties = new LinkedHashMap<>();
    private List<Node> nodes = new ArrayList<>();

    public RevisionImpl(int number) {
        this(number, null);
    }

    public RevisionImpl(int number, String date) {
        this.number = number;
        if(date != null) {
            this.properties.put(Property.DATE, date);
        }
    }

    public RevisionImpl(Revision revision) {
        this.number = revision.getNumber();
        this.properties = new LinkedHashMap<>(revision.getProperties());
        for(Node node : revision.getNodes()) {
            nodes.add(new NodeImpl(node));
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
            throw new NullPointerException("Cannot set null properties on Revision.");
        }
        this.properties = new LinkedHashMap<>(properties);
    }

    @Override
    public List<Node> getNodes() {
        return nodes;
    }

    @Override
    public void addNode(Node node) {
        if(node == null) {
            throw new NullPointerException("Cannot add null node to Revision.");
        }
        nodes.add(node);
    }

    @Override
    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Override
    public String toString() {
        String date = get(Property.DATE);
        String author = get(Property.AUTHOR);
        String log = get(Property.LOG);

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
