package com.github.cstroe.svndumpgui.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public interface SvnDumpWriter extends SvnDumpConsumer {
    void writeTo(OutputStream os);

    default void write(OutputStream os, SvnDump dump) throws IOException {
        writeTo(os);
        consume(dump.getPreamble());
        Iterator<SvnRevision> revisionIter = dump.revisions();
        while(revisionIter.hasNext()) {
            SvnRevision revision = revisionIter.next();
            consume(revision);

            Iterator<SvnNode> nodeIter = revision.nodes();
            while(nodeIter.hasNext()) {
                consume(nodeIter.next());
            }
        }
        finish();
    }
}
