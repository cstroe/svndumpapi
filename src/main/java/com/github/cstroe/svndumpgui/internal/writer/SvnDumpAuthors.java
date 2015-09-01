package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.SortedSet;
import java.util.TreeSet;

public class SvnDumpAuthors implements SvnDumpWriter {
    private SortedSet<String> authors = new TreeSet<>();

    @Override
    public void writePreamble(OutputStream os, SvnDump dump) throws IOException {}

    @Override
    public void writeRevision(OutputStream os, SvnRevision revision) throws IOException {
        if(revision.getProperties().containsKey(SvnProperty.AUTHOR)) {
            authors.add(revision.get(SvnProperty.AUTHOR));
        }
    }

    @Override
    public void finish(OutputStream os) {
        PrintStream ps = new PrintStream(os);
        for(String author : authors) {
            ps.println(author);
        }
        authors = new TreeSet<>();
    }
}
