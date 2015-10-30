package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.internal.AbstractRepositoryConsumer;
import com.github.cstroe.svndumpgui.internal.SimplePrintStream;

import java.io.OutputStream;

public abstract class AbstractRepositoryWriter extends AbstractRepositoryConsumer implements RepositoryWriter {
    private SimplePrintStream ps;

    @Override
    public void writeTo(OutputStream os) {
        if(os == null) {
            throw new IllegalArgumentException("Cannot write to null output stream.");
        }
        this.ps = new SimplePrintStream(os);
    }

    public SimplePrintStream ps() {
        return ps;
    }
}
