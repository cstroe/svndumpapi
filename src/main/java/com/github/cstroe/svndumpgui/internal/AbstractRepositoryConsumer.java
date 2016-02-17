package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Revision;

/**
 * Responsible for continuing the consumer chain processing.
 * Uses Abstract prefix in name to follow the naming scheme.
 */
public abstract class AbstractRepositoryConsumer implements RepositoryConsumer {
    private RepositoryConsumer previousConsumer;
    private RepositoryConsumer nextConsumer;

    @Override
    public RepositoryConsumer getNextConsumer() {
        return nextConsumer;
    }

    @Override
    public void setNextConsumer(RepositoryConsumer consumer) {
        this.nextConsumer = consumer;
    }

    @Override
    public RepositoryConsumer getPreviousConsumer() {
        return previousConsumer;
    }

    @Override
    public void setPreviousConsumer(RepositoryConsumer previousConsumer) {
        this.previousConsumer = previousConsumer;
    }

    @Override
    public void consume(Preamble preamble) {
        if(nextConsumer != null) {
            nextConsumer.consume(preamble);
        }
    }

    @Override
    public void consume(Revision revision) {
        if(nextConsumer != null) {
            nextConsumer.consume(revision);
        }
    }

    @Override
    public void endRevision(Revision revision) {
        if(nextConsumer != null) {
            nextConsumer.endRevision(revision);
        }
    }

    @Override
    public void consume(Node node) {
        if(nextConsumer != null) {
            nextConsumer.consume(node);
        }
    }

    @Override
    public void endNode(Node node) {
        if(nextConsumer != null) {
            nextConsumer.endNode(node);
        }
    }

    @Override
    public void consume(ContentChunk chunk) {
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
