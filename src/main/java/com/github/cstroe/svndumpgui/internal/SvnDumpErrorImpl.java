package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDumpError;
import com.github.cstroe.svndumpgui.api.SvnNode;

public class SvnDumpErrorImpl implements SvnDumpError {
    private final String message;
    private final int revision;
    private final SvnNode node;

    public SvnDumpErrorImpl(String message, int revision, SvnNode node) {
        this.message = message;
        this.revision = revision;
        this.node = node;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getRevision() {
        return revision;
    }

    @Override
    public SvnNode getNode() {
        return node;
    }
}
