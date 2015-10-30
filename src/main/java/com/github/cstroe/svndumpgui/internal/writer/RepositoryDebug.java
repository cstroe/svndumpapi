package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Revision;

import java.io.OutputStream;

public class RepositoryDebug extends AbstractRepositoryWriter {

    @SuppressWarnings("unused")
    public RepositoryDebug() {
        this(System.err);
    }

    public RepositoryDebug(OutputStream outputStream) {
        super();
        writeTo(outputStream);
    }

    @Override
    public void consume(Preamble preamble) {
        ps().println("consume(" + preamble.toString() + ")");
        super.consume(preamble);
    }

    @Override
    public void consume(Revision revision) {
        ps().println("consume(" + revision.toString() + ")");
        super.consume(revision);
    }

    @Override
    public void endRevision(Revision revision) {
        ps().println("endRevision(" + revision.toString() + ")");
        super.endRevision(revision);
    }

    @Override
    public void consume(Node node) {
        ps().println("consume(" + node.toString() + ")");
        super.consume(node);
    }

    @Override
    public void endNode(Node node) {
        ps().println("endNode(" + node.toString() + ")");
        super.endNode(node);
    }

    @Override
    public void consume(ContentChunk chunk) {
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
