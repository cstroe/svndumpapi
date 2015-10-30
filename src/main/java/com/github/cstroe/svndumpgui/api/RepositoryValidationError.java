package com.github.cstroe.svndumpgui.api;

public interface RepositoryValidationError {
    String getMessage();
    int getRevision();
    Node getNode();
}
