package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class SvnDumpDebug extends AbstractSvnDumpWriter {

    @Override
    public void consume(SvnDumpPreamble preamble) {
        System.err.println("consume(" + preamble.toString() + ")");
        super.consume(preamble);
    }

    @Override
    public void consume(SvnRevision revision) {
        System.err.println("consume(" + revision.toString() + ")");
        super.consume(revision);
    }

    @Override
    public void endRevision(SvnRevision revision) {
        System.err.println("endRevision(" + revision.toString() + ")");
        super.endRevision(revision);
    }

    @Override
    public void consume(SvnNode node) {
        System.err.println("consume(" + node.toString() + ")");
        super.consume(node);
    }

    @Override
    public void endNode(SvnNode node) {
        System.err.println("endNode(" + node.toString() + ")");
        super.endNode(node);
    }

    @Override
    public void consume(FileContentChunk chunk) {
        System.err.println("consume(" + chunk.toString() + ") " + chunk.getContent().length);
        super.consume(chunk);
    }

    @Override
    public void endChunks() {
        System.err.println("endChunks()");
        super.endChunks();
    }

    @Override
    public void finish() {
        System.err.println("finish()");
        super.finish();
    }
}
