package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.RepositoryValidationError;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.RepositoryValidator;
import com.github.cstroe.svndumpgui.api.Revision;

public class TerminatingValidator extends AbstractRepositoryValidator {

    private final RepositoryValidator wrappedValidator;

    public TerminatingValidator(RepositoryValidator wrappedValidator) {
        this.wrappedValidator = wrappedValidator;
    }

    @Override
    public boolean isValid() {
        return wrappedValidator.isValid();
    }

    @Override
    public RepositoryValidationError getError() {
        return wrappedValidator.getError();
    }

    @Override
    public void consume(Preamble preamble) {
        wrappedValidator.consume(preamble);
        ensureValid();
    }

    @Override
    public void consume(Revision revision) {
        wrappedValidator.consume(revision);
        ensureValid();
    }

    @Override
    public void finish() {
        wrappedValidator.finish();
        ensureValid();
    }

    private void ensureValid() {
        if(!wrappedValidator.isValid()) {
            throw new AssertionError(wrappedValidator.getError().getMessage());
        }
    }
}
