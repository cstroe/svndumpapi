package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.Revision;

public class RepositoryRevisions extends AbstractRepositoryWriter {

    @Override
    public void consume(Revision revision) {
        ps().print("Finished revision ");
        ps().print(revision.getNumber());
        ps().println(".");
        super.consume(revision);
    }
}
