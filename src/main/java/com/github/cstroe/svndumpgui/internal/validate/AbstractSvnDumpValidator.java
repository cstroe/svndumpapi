package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public abstract class AbstractSvnDumpValidator implements SvnDumpValidator {
    @Override
    public void consume(SvnDumpPreamble preamble) {}

    @Override
    public void consume(SvnRevision revision) {}

    @Override
    public void finish() {}
}
