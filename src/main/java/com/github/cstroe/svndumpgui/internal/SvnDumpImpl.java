package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SvnDumpImpl implements SvnDump {

    private SvnDumpPreamble preamble;
    private List<SvnRevision> revisions = new ArrayList<>();

    @Override
    public SvnDumpPreamble getPreamble() {
        return preamble;
    }

    @Override
    public void setPreamble(SvnDumpPreamble preamble) {
        this.preamble = preamble;
    }

    public void addRevision(SvnRevision revision) {
        revisions.add(revision);
    }

    @Override
    public List<SvnRevision> getRevisions() {
        return revisions;
    }

    @Override
    public Iterator<SvnRevision> revisions() {
        return revisions.iterator();
    }
}
