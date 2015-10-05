package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnRevision;

public class SvnDumpRevisions extends AbstractSvnDumpWriter {

    @Override
    public void consume(SvnRevision revision) {
        ps().print("Finished revision ");
        ps().print(revision.getNumber());
        ps().println(".");
        super.consume(revision);
    }
}
