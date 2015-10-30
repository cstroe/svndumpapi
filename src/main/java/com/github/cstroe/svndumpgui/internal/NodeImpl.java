package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class NodeImpl implements Node {
    private Optional<Revision> revision;
    private Map<NodeHeader, String> headers = new LinkedHashMap<>();
    private Map<String, String> properties = new LinkedHashMap<>();
    private List<ContentChunk> content = new LinkedList<>();

    public NodeImpl() {
        this.revision = Optional.empty();
    }

    public NodeImpl(Revision revision) {
        this.revision = Optional.ofNullable(revision);
    }

    public NodeImpl(Node node) {
        this.revision = Optional.ofNullable(node.getRevision().orElse(null));
        this.headers = new LinkedHashMap<>(node.getHeaders());
        if(node.getProperties() != null) {
            this.properties = new LinkedHashMap<>(node.getProperties());
        }

        List<ContentChunk> nodeContent = node.getContent();
        content = new ArrayList<>(nodeContent.size());
        for(ContentChunk nodeChunk : nodeContent) {
            content.add(new ContentChunkImpl(nodeChunk));
        }
    }

    @Override
    public Optional<Revision> getRevision() {
        return revision;
    }

    @Override
    public void setRevision(Revision revision) {
        this.revision = Optional.of(revision);
    }

    @Override
    public List<ContentChunk> getContent() {
        return Collections.unmodifiableList(content);
    }

    @Override
    public void addFileContentChunk(ContentChunk chunk) {
        content.add(chunk);
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public void setHeaders(Map<NodeHeader, String> headers) {
        this.headers = headers;
    }

    @Override
    public Map<NodeHeader, String> getHeaders() {
        return headers;
    }

    @Override
    public String get(NodeHeader header) {
        return headers.get(header);
    }

    @Override
    public String toString() {
        String md5hash = "";
        if(headers.containsKey(NodeHeader.MD5)) {
            md5hash += " " + headers.get(NodeHeader.MD5);
        }
        String copyInfo = md5hash;
        if(headers.containsKey(NodeHeader.COPY_FROM_PATH)) {
            String copyFromRevision = headers.get(NodeHeader.COPY_FROM_REV);
            copyInfo += " -- copied from: " + headers.get(NodeHeader.COPY_FROM_PATH) + "@" + copyFromRevision;
            if(headers.containsKey(NodeHeader.SOURCE_MD5)) {
                copyInfo += " " + headers.get(NodeHeader.SOURCE_MD5);
            }
        }

        String sizeInfo = "";
        if(headers.containsKey(NodeHeader.TEXT_CONTENT_LENGTH)) {
            sizeInfo = " Size: " + headers.get(NodeHeader.TEXT_CONTENT_LENGTH) + " bytes";
        }

        if(headers.containsKey(NodeHeader.KIND)) {
            return  headers.get(NodeHeader.ACTION) + " " +
                    headers.get(NodeHeader.KIND) + " " +
                    headers.get(NodeHeader.PATH) + copyInfo + sizeInfo;
        } else {
            return  headers.get(NodeHeader.ACTION) + " " +
                    headers.get(NodeHeader.PATH) + copyInfo + sizeInfo;
        }
    }
}
