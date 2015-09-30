package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NoopSvnDumpMutator implements SvnDumpMutator {
    @Override
    public void consume(SvnDumpPreamble preamble) {}

    @Override
    public void consume(SvnRevision revision) {}

    @Override
    public void finish() {}
}
