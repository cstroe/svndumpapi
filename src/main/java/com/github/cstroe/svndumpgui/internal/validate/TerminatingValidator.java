package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDumpValidationError;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class TerminatingValidator extends AbstractSvnDumpValidator {

    private final SvnDumpValidator wrappedValidator;

    public TerminatingValidator(SvnDumpValidator wrappedValidator) {
        this.wrappedValidator = wrappedValidator;
    }

    @Override
    public boolean isValid() {
        return wrappedValidator.isValid();
    }

    @Override
    public SvnDumpValidationError getError() {
        return wrappedValidator.getError();
    }

    @Override
    public void consume(SvnDumpPreamble preamble) {
        wrappedValidator.consume(preamble);
        ensureValid();
    }

    @Override
    public void consume(SvnRevision revision) {
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
