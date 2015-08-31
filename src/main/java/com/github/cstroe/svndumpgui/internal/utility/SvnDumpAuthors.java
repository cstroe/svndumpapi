package com.github.cstroe.svndumpgui.internal.utility;

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
    @Override
    public void write(OutputStream os, SvnDump dump) throws IOException {
        SortedSet<String> authors = new TreeSet<>();
        for(SvnRevision revision : dump.getRevisions()) {
            if(revision.getProperties().containsKey(SvnProperty.AUTHOR)) {
                authors.add(revision.get(SvnProperty.AUTHOR));
            }
        }

        PrintStream ps = new PrintStream(os);
        for(String author : authors) {
            ps.println(author);
        }
    }
}
