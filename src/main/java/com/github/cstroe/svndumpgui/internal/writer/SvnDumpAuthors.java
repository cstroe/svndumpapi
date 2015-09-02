package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.io.PrintStream;
import java.util.SortedSet;
import java.util.TreeSet;

public class SvnDumpAuthors extends AbstractSvnDumpWriter {
    private SortedSet<String> authors = new TreeSet<>();

    @Override
    public void consumePreamble(SvnDump dump) {}

    @Override
    public void consumeRevision(SvnRevision revision) {
        if(revision.getProperties().containsKey(SvnProperty.AUTHOR)) {
            authors.add(revision.get(SvnProperty.AUTHOR));
        }
    }

    @Override
    public void finish() {
        PrintStream ps = new PrintStream(getOutputStream());
        for(String author : authors) {
            ps.println(author);
        }
        authors = new TreeSet<>();
    }
}
