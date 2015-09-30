package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class UpdateAuthorForEmptyRevisions extends NoopSvnDumpMutator {
    private final String newAuthor;

    public UpdateAuthorForEmptyRevisions(String newAuthor) {
        this.newAuthor = newAuthor;
    }

    @Override
    public void consume(SvnRevision revision) {
        if(revision.getProperties().containsKey(SvnProperty.AUTHOR) && revision.getNodes().isEmpty() ) {
            revision.getProperties().put(SvnProperty.AUTHOR, newAuthor);
        }
    }
}
