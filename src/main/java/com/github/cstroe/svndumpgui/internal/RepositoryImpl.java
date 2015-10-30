package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.Revision;

import java.util.ArrayList;
import java.util.List;

public class RepositoryImpl implements Repository {

    private Preamble preamble;
    private List<Revision> revisions = new ArrayList<>();

    @Override
    public Preamble getPreamble() {
        return preamble;
    }

    @Override
    public void setPreamble(Preamble preamble) {
        this.preamble = preamble;
    }

    public void addRevision(Revision revision) {
        revisions.add(revision);
    }

    @Override
    public List<Revision> getRevisions() {
        return revisions;
    }
}
