package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDumpWriter;

import java.io.OutputStream;

public abstract class AbstractSvnDumpWriter implements SvnDumpWriter {
    private OutputStream os;

    @Override
    public void writeTo(OutputStream os) {
        this.os = os;
    }

    public OutputStream getOutputStream() {
        return os;
    }
}
