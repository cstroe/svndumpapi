package com.github.cstroe.svndumpgui.api.mutable;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.List;
import java.util.Map;

public interface MutableSvnRevision extends SvnRevision {
    public void setNumber(int number);
    public void setProperties(Map<String, String> properties);
    public void setNodes(List<SvnNode> nodes);
}
