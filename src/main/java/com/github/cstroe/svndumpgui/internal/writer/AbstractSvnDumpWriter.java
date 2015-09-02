package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;

import java.io.OutputStream;
import java.io.PrintStream;

public abstract class AbstractSvnDumpWriter implements SvnDumpWriter {
    private OutputStream os;
    private PrintStream ps;

    @Override
    public void writeTo(OutputStream os) {
        this.os = os;
        this.ps = new PrintStream(os);
    }

    @SuppressWarnings("unused")
    public OutputStream os() {
        return os;
    }

    public PrintStream ps() {
        return ps;
    }

    @Override
    public void consume(SvnDumpPreamble preamble) {}

    @Override
    public void finish() {}
}
