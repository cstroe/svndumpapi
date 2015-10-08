package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

/**
 * Responsible for continuing the consumer chain processing.
 * Uses Abstract prefix in name to follow the naming scheme.
 */
public class AbstractSvnDumpConsumer implements SvnDumpConsumer {
    private SvnDumpConsumer nextConsumer;

    @Override
    public void continueTo(SvnDumpConsumer nextConsumer) {
        this.nextConsumer = nextConsumer;
    }

    @Override
    public SvnDumpConsumer getNextConsumer() {
        return nextConsumer;
    }

    @Override
    public void setNextConsumer(SvnDumpConsumer consumer) {
        this.nextConsumer = consumer;
    }

    @Override
    public void consume(SvnDumpPreamble preamble) {
        if(nextConsumer != null) {
            nextConsumer.consume(preamble);
        }
    }

    @Override
    public void consume(SvnRevision revision) {
        if(nextConsumer != null) {
            nextConsumer.consume(revision);
        }
    }

    @Override
    public void endRevision(SvnRevision revision) {
        if(nextConsumer != null) {
            nextConsumer.endRevision(revision);
        }
    }

    @Override
    public void consume(SvnNode node) {
        if(nextConsumer != null) {
            nextConsumer.consume(node);
        }
    }

    @Override
    public void endNode(SvnNode node) {
        if(nextConsumer != null) {
            nextConsumer.endNode(node);
        }
    }

    @Override
    public void consume(FileContentChunk chunk) {
        if(nextConsumer != null) {
            nextConsumer.consume(chunk);
        }
    }

    @Override
    public void endChunks() {
        if(nextConsumer != null) {
            nextConsumer.endChunks();
        }
    }

    @Override
    public void finish() {
        if(nextConsumer != null) {
            nextConsumer.finish();
        }
    }
}
