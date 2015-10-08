package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SvnNodeImpl implements SvnNode {
    private Optional<SvnRevision> revision;
    private Map<SvnNodeHeader, String> headers = new LinkedHashMap<>();
    private Map<String, String> properties = new LinkedHashMap<>();
    private List<FileContentChunk> content = new LinkedList<>();

    public SvnNodeImpl() {
        this.revision = Optional.empty();
    }

    public SvnNodeImpl(SvnRevision revision) {
        this.revision = Optional.ofNullable(revision);
    }

    public SvnNodeImpl(SvnNode node) {
        this.revision = Optional.ofNullable(node.getRevision().orElse(null));
        this.headers = new LinkedHashMap<>(node.getHeaders());
        if(node.getProperties() != null) {
            this.properties = new LinkedHashMap<>(node.getProperties());
        }

        List<FileContentChunk> nodeContent = node.getContent();
        content = new ArrayList<>(nodeContent.size());
        for(FileContentChunk nodeChunk : nodeContent) {
            content.add(new FileContentChunkImpl(nodeChunk));
        }
    }

    @Override
    public Optional<SvnRevision> getRevision() {
        return revision;
    }

    @Override
    public void setRevision(SvnRevision revision) {
        this.revision = Optional.of(revision);
    }

    @Override
    public List<FileContentChunk> getContent() {
        return Collections.unmodifiableList(content);
    }

    @Override
    public void addFileContentChunk(FileContentChunk chunk) {
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
    public void setHeaders(Map<SvnNodeHeader, String> headers) {
        this.headers = headers;
    }

    @Override
    public Map<SvnNodeHeader, String> getHeaders() {
        return headers;
    }

    @Override
    public String get(SvnNodeHeader header) {
        return headers.get(header);
    }

    @Override
    public String toString() {
        String md5hash = "";
        if(headers.containsKey(SvnNodeHeader.MD5)) {
            md5hash += " " + headers.get(SvnNodeHeader.MD5);
        }
        String copyInfo = md5hash;
        if(headers.containsKey(SvnNodeHeader.COPY_FROM_PATH)) {
            String copyFromRevision = headers.get(SvnNodeHeader.COPY_FROM_REV);
            copyInfo += " -- copied from: " + headers.get(SvnNodeHeader.COPY_FROM_PATH) + "@" + copyFromRevision;
            if(headers.containsKey(SvnNodeHeader.SOURCE_MD5)) {
                copyInfo += " " + headers.get(SvnNodeHeader.SOURCE_MD5);
            }
        }
        if(headers.containsKey(SvnNodeHeader.KIND)) {
            return  headers.get(SvnNodeHeader.ACTION) + " " +
                    headers.get(SvnNodeHeader.KIND) + " " +
                    headers.get(SvnNodeHeader.PATH) + copyInfo;
        } else {
            return  headers.get(SvnNodeHeader.ACTION) + " " +
                    headers.get(SvnNodeHeader.PATH) + copyInfo;
        }
    }
}
