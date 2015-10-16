package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.io.OutputStream;

public class SvnDumpDebug extends AbstractSvnDumpWriter {

    @SuppressWarnings("unused")
    public SvnDumpDebug() {
        this(System.err);
    }

    public SvnDumpDebug(OutputStream outputStream) {
        super();
        writeTo(outputStream);
    }

    @Override
    public void consume(SvnDumpPreamble preamble) {
        ps().println("consume(" + preamble.toString() + ")");
        super.consume(preamble);
    }

    @Override
    public void consume(SvnRevision revision) {
        ps().println("consume(" + revision.toString() + ")");
        super.consume(revision);
    }

    @Override
    public void endRevision(SvnRevision revision) {
        ps().println("endRevision(" + revision.toString() + ")");
        super.endRevision(revision);
    }

    @Override
    public void consume(SvnNode node) {
        ps().println("consume(" + node.toString() + ")");
        super.consume(node);
    }

    @Override
    public void endNode(SvnNode node) {
        ps().println("endNode(" + node.toString() + ")");
        super.endNode(node);
    }

    @Override
    public void consume(FileContentChunk chunk) {
        ps().println("consume(" + chunk.toString() + ")");
        super.consume(chunk);
    }

    @Override
    public void endChunks() {
        ps().println("endChunks()");
        super.endChunks();
    }

    @Override
    public void finish() {
        ps().println("finish()");
        super.finish();
    }
}
