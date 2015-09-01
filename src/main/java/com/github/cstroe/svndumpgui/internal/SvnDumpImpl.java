package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SvnDumpImpl implements SvnDump {

    private String uuid;
    private List<SvnRevision> revisions = new ArrayList<>();

    @Override
    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
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
