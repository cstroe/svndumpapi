package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpPreambleImpl;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;

public class SvnDumpInMemory extends AbstractSvnDumpWriter {

    private SvnDump dump;
    private SvnRevision currentRevision;

    @Override
    public void consume(SvnDumpPreamble preamble) {
        dump = new SvnDumpImpl();
        dump.setPreamble(new SvnDumpPreambleImpl(preamble));
        super.consume(preamble);
    }

    @Override
    public void consume(SvnRevision revision) {
        currentRevision = new SvnRevisionImpl(revision);
        dump.getRevisions().add(currentRevision);
        super.consume(revision);
    }

    @Override
    public void consume(SvnNode node) {
        SvnNodeImpl nodeCopy = new SvnNodeImpl(node);
        nodeCopy.setRevision(currentRevision);
        currentRevision.addNode(nodeCopy);
        super.consume(node);
    }

    public SvnDump getDump() {
        return dump;
    }
}
