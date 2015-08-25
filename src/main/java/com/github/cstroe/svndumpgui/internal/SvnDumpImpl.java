package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.List;

public class SvnDumpImpl implements SvnDump {

    private List<SvnRevision> revisions = new ArrayList<>();

    @Override
    public void addRevision(SvnRevision revision) {
        revisions.add(revision);
    }

    @Override
    public List<SvnRevision> getRevisions() {
        List<SvnRevision> revisionList = new ArrayList<>();
        revisionList.add(new SvnRevisionImpl());
        return revisionList;
    }
}
