package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class UpdateAuthorForEmptyRevisions implements SvnDumpMutator {
    private final String newAuthor;

    public UpdateAuthorForEmptyRevisions(String newAuthor) {
        this.newAuthor = newAuthor;
    }

    @Override
    public void consume(SvnDumpPreamble dump) {}

    @Override
    public void consume(SvnRevision revision) {
        if(revision.getProperties().containsKey(SvnProperty.AUTHOR) && revision.getNodes().isEmpty() ) {
            revision.getProperties().put(SvnProperty.AUTHOR, newAuthor);
        }
    }

    @Override
    public void finish() {}
}
