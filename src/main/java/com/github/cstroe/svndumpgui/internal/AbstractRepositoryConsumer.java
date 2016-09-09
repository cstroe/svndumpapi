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
    public void consume(Preamble preamble) {}

    @Override
    public void consume(Revision revision) {}

    @Override
    public void endRevision(Revision revision) {}

    @Override
    public void consume(Node node) {}

    @Override
    public void endNode(Node node) {}

    @Override
    public void consume(ContentChunk chunk) {}

    @Override
    public void endChunks() {}

    @Override
    public void finish() {}
}
