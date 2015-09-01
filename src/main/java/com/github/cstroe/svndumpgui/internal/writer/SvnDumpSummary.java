package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.*;

import java.io.IOException;
import java.io.PrintStream;

public class SvnDumpSummary extends AbstractSvnDumpWriter {
    private static final int NOT_SET = -1;

    private int firstEmptyRevision = NOT_SET;
    private int lastEmptyRevision = NOT_SET;

    @Override
    public void consumePreamble(SvnDump dump) throws IOException {}

    @Override
    public void consumeRevision(SvnRevision revision) throws IOException {
        PrintStream ps = new PrintStream(getOutputStream());
        if(revision.getNodes().isEmpty()) {
            if(firstEmptyRevision == NOT_SET) {
                firstEmptyRevision = revision.getNumber();
            }
            lastEmptyRevision = revision.getNumber();
            return;
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

    @Override
    public void finish() {}
}
