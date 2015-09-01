package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.*;

public class UpdateAuthorForEmptyRevisions extends AbstractSvnDumpMutator {
    private final String newAuthor;

    public UpdateAuthorForEmptyRevisions(String newAuthor) {
        this.newAuthor = newAuthor;
    }

    @Override
    public void mutate(SvnRevision revision) {
        if(revision.getProperties().containsKey(SvnProperty.AUTHOR) && revision.getNodes().isEmpty() ) {
            revision.getProperties().put(SvnProperty.AUTHOR, newAuthor);
        }
    }
}
