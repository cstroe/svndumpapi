package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;

import java.util.LinkedHashMap;
import java.util.Map;

public class SvnNodeImpl implements SvnNode {
    private Map<SvnNodeHeader, String> headers = new LinkedHashMap<>();
    private Map<String, String> properties;
    private byte[] content;

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

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
        String copyInfo = "";
        if(headers.containsKey(SvnNodeHeader.COPY_FROM_PATH)) {
            copyInfo = " -- copied from: " + headers.get(SvnNodeHeader.COPY_FROM_PATH);
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
