package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.List;

public class MutatorChain implements SvnDumpMutator {

    private List<SvnDumpMutator> mutators = new ArrayList<>();

    public void add(SvnDumpMutator mutator) {
        mutators.add(mutator);
    }

    @Override
    public void mutateRevision(SvnRevision revision) {
        for(SvnDumpMutator mutator : mutators) {
            mutator.mutateRevision(revision);
        }
    }

    @Override
    public void finish() {
        for(SvnDumpMutator mutator : mutators) {
            mutator.finish();
        }
    }
}
