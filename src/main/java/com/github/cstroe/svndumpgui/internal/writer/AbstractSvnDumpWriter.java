package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.internal.AbstractSvnDumpConsumer;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class AbstractSvnDumpWriter extends AbstractSvnDumpConsumer implements SvnDumpWriter {
    private PrintStream ps;

    @Override
    public void writeTo(OutputStream os) {
        this.ps = new PrintStream(os);
    }

    public PrintStream ps() {
        assert ps != null;
        return ps;
    }
}
