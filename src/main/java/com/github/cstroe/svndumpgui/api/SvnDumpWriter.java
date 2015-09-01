package com.github.cstroe.svndumpgui.api;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

public interface SvnDumpWriter {
    void writePreamble(OutputStream os, SvnDump dump) throws IOException;
    void writeRevision(OutputStream os, SvnRevision revision) throws IOException;
    void finish(OutputStream os);

    default void write(OutputStream os, SvnDump dump) throws IOException {
        writePreamble(os, dump);
        Iterator<SvnRevision> revisionIter = dump.revisions();
        while(revisionIter.hasNext()) {
            writeRevision(os, revisionIter.next());
        }
        finish(os);
    }
}
