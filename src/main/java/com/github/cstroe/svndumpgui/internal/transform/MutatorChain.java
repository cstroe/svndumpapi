package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.List;

public class MutatorChain extends AbstractSvnDumpMutator {

    private List<SvnDumpMutator> mutators = new ArrayList<>();

    public void add(SvnDumpMutator mutator) {
        mutators.add(mutator);
    }

    @Override
    public void mutate(SvnRevision revision) {
        for(SvnDumpMutator mutator : mutators) {
            mutator.mutate(revision);
        }
    }

    @Override
    public void finish() {
        for(SvnDumpMutator mutator : mutators) {
            mutator.finish();
        }
    }
}
