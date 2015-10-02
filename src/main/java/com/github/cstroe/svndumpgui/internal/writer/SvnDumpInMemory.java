package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;

public class SvnDumpInMemory extends AbstractSvnDumpWriter {

    private SvnDump dump;

    @Override
    public void consume(SvnDumpPreamble preamble) {
        dump = new SvnDumpImpl();
        dump.setPreamble(preamble);
        super.consume(preamble);
    }

    @Override
    public void consume(SvnRevision revision) {
        dump.getRevisions().add(revision);
        super.consume(revision);
    }

    @Override
    public void consume(SvnNode node) {
        node.getRevision().get().addNode(node);
        super.consume(node);
    }

    public SvnDump getDump() {
        return dump;
    }
}
