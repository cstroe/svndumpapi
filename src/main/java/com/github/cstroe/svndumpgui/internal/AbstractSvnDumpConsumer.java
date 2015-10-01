package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

/**
 * Responsible for continuing the consumer chain processing.
 */
public abstract class AbstractSvnDumpConsumer implements SvnDumpConsumer {
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
    public void consume(SvnNode node) {
        if(nextConsumer != null) {
            nextConsumer.consume(node);
        }
    }

    @Override
    public void finish() {
        if(nextConsumer != null) {
            nextConsumer.finish();
        }
    }
}
