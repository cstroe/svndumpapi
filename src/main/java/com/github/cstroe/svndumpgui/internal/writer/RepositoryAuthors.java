package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;

import java.util.HashSet;
import java.util.Set;

public class RepositoryAuthors extends AbstractRepositoryWriter {
    private Set<String> authors = new HashSet<>();

    @Override
    public void consume(Revision revision) {
        String author = revision.get(Property.AUTHOR);
        if(author != null && !authors.contains(author)) {
            ps().println(author);
            authors.add(author);
        }
        super.consume(revision);
    }
}
