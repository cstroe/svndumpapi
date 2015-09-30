package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class SvnNodeImpl implements SvnNode {
    private final Optional<SvnRevision> revision;
    private Map<SvnNodeHeader, String> headers = new LinkedHashMap<>();
    private Map<String, String> properties;
    private byte[] content;

    public SvnNodeImpl(SvnRevision revision) {
        this.revision = Optional.ofNullable(revision);
    }

    @Override
    public Optional<SvnRevision> getRevision() {
        return revision;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void setContent(byte[] content) {
        this.content = content;
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
                copyInfo += " " + headers.containsKey(SvnNodeHeader.SOURCE_MD5);
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
