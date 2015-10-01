package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.AbstractSvnDumpConsumer;

public abstract class AbstractSvnDumpValidator extends AbstractSvnDumpConsumer implements SvnDumpValidator {
    @Override
    public void consume(SvnDumpPreamble preamble) {}

    @Override
    public void consume(SvnRevision revision) {}

    @Override
    public void consume(SvnNode node) {}

    @Override
    public void finish() {}
}
