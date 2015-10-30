package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.RepositoryValidationError;

public class RepositoryValidationErrorImpl implements RepositoryValidationError {
    private final String message;
    private final int revision;
    private final Node node;

    public RepositoryValidationErrorImpl(String message, int revision, Node node) {
        this.message = message;
        this.revision = revision;
        this.node = node;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getRevision() {
        return revision;
    }

    @Override
    public Node getNode() {
        return node;
    }
}
