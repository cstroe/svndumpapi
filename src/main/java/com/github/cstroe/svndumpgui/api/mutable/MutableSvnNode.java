package com.github.cstroe.svndumpgui.api.mutable;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;

import java.util.Map;

public interface MutableSvnNode extends SvnNode {
    void setContent(byte[] content);
    void setProperties(Map<String, String> properties);
    void setHeaders(Map<SvnNodeHeader, String> headers);
}
