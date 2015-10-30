package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;

public class RepositorySummary extends AbstractRepositoryWriter {
    private static final int NOT_SET = -1;

    private int firstEmptyRevision = NOT_SET;
    private int lastEmptyRevision = NOT_SET;

    private boolean currentRevisionIsEmpty;
    private boolean revisionHeaderPrinted = false;

    @Override
    public void consume(Revision revision) {
        revisionHeaderPrinted = false;
        super.consume(revision);
    }

    @Override
    public void endRevision(Revision revision) {
        if(!revisionHeaderPrinted) { // revision is empty
            if(firstEmptyRevision == NOT_SET) {
                firstEmptyRevision = revision.getNumber();
            }
            lastEmptyRevision = revision.getNumber();
        } else {
            ps().println();
        }

        super.endRevision(revision);
    }

    @Override
    public void consume(Node node) {
        if(!revisionHeaderPrinted) {
            printRevisionHeader(node.getRevision().get());
        }

        ps().println("\t" + node.toString());
        super.consume(node);
    }

    private void printRevisionHeader(Revision revision) {
        // check for previously empty revisions
        if(firstEmptyRevision != NOT_SET) {
            if (firstEmptyRevision != lastEmptyRevision) {
                ps().println("r" + firstEmptyRevision + "-" + lastEmptyRevision + ": **empty**\n");
            } else {
                ps().println("r" + firstEmptyRevision + ": **empty**\n");
            }

            firstEmptyRevision = NOT_SET;
            lastEmptyRevision = NOT_SET;
        }

        // print current revision
        String date = String.valueOf(revision.get(Property.DATE));
        ps().println("r" + revision.getNumber() + ": " +
                String.valueOf(revision.get(Property.LOG)).trim() + " - " +
                String.valueOf(revision.get(Property.AUTHOR)) + " " +
                date.substring(0, Math.min(date.length(), 10)));
        ps().println();

        revisionHeaderPrinted = true;
    }
}
