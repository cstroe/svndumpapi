package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.HashSet;
import java.util.Set;

public class SvnDumpAuthors extends AbstractSvnDumpWriter {
    private Set<String> authors = new HashSet<>();

    @Override
    public void consume(SvnRevision revision) {
        String author = revision.get(SvnProperty.AUTHOR);
        if(author != null && !authors.contains(author)) {
            ps().println(author);
            authors.add(author);
        }
    }
}
