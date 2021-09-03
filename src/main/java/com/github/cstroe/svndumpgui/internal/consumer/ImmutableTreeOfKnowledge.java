package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.*;
import com.github.cstroe.svndumpgui.internal.AbstractRepositoryConsumer;

public class ImmutableTreeOfKnowledge extends AbstractRepositoryConsumer implements TreeOfKnowledge {
    private static final String CANNOT_MUTATE = "Cannot mutate immutable object";
    private TreeOfKnowledge tok;

    public ImmutableTreeOfKnowledge(TreeOfKnowledge tok) {
        this.tok = tok;
    }

    @Override
    public Node tellMeAbout(int revision, String path) {
        return tok.tellMeAbout(revision, path);
    }

    @Override
    public void setNextConsumer(RepositoryConsumer consumer) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void setPreviousConsumer(RepositoryConsumer previousConsumer) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void consume(Preamble preamble) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void consume(Revision revision) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void endRevision(Revision revision) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void consume(Node node) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void endNode(Node node) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void consume(ContentChunk chunk) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void endChunks() {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }

    @Override
    public void continueTo(RepositoryConsumer nextConsumer) {
        throw new UnsupportedOperationException(CANNOT_MUTATE);
    }
}
