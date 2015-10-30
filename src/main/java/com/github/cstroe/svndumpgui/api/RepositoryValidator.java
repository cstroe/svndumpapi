package com.github.cstroe.svndumpgui.api;


public interface RepositoryValidator extends RepositoryConsumer {
    boolean isValid();
    RepositoryValidationError getError();
}
