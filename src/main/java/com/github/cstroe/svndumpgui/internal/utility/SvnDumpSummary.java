package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.*;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class SvnDumpSummary implements SvnDumpWriter {
    private static final int NOT_SET = -1;

    @Override
    public void write(OutputStream os, SvnDump dump) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(os);
        PrintStream ps = new PrintStream(bos);
        int firstEmptyRevision = NOT_SET;
        int lastEmptyRevision = NOT_SET;
        for(SvnRevision revision : dump.getRevisions()) {
            if(revision.getNodes().isEmpty()) {
                if(firstEmptyRevision == NOT_SET) {
                    firstEmptyRevision = revision.getNumber();
                }
                lastEmptyRevision = revision.getNumber();
                continue;
            } else if(firstEmptyRevision != NOT_SET) {
                // no longer have empty revisions
                if(firstEmptyRevision != lastEmptyRevision) {
                    ps.println("r" + firstEmptyRevision + "-" + lastEmptyRevision + ": **empty**\n");
                } else {
                    ps.println("r" + firstEmptyRevision + ": **empty**\n");
                }
                firstEmptyRevision = NOT_SET;
                lastEmptyRevision = NOT_SET;
            }

            String date = String.valueOf(revision.get(SvnProperty.DATE));
            ps.println("r" + revision.getNumber() + ": " +
                    String.valueOf(revision.get(SvnProperty.LOG)).trim() + " - " +
                    String.valueOf(revision.get(SvnProperty.AUTHOR)) + " " +
                    date.substring(0, Math.min(date.length(), 10))  );
            ps.println();
            for(SvnNode node : revision.getNodes()) {
                ps.println("\t" + node.toString());
            }
            ps.println();
        }
        ps.close();
    }
}
