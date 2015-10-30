package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;

public class UpdateAuthorForEmptyRevisions extends AbstractRepositoryMutator {
    private final String newAuthor;

    public UpdateAuthorForEmptyRevisions(String newAuthor) {
        this.newAuthor = newAuthor;
    }

    @Override
    public void consume(Revision revision) {
        if(revision.getProperties().containsKey(Property.AUTHOR) && revision.getNodes().isEmpty() ) {
            revision.getProperties().put(Property.AUTHOR, newAuthor);
        }
        super.consume(revision);
    }
}
