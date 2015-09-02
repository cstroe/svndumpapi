package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.ArrayList;
import java.util.List;

public class MutatorChain implements SvnDumpMutator {

    private List<SvnDumpMutator> mutators = new ArrayList<>();

    public void add(SvnDumpMutator mutator) {
        mutators.add(mutator);
    }

    @Override
    public void consume(SvnDumpPreamble preamble) {
        for(SvnDumpMutator mutator : mutators) {
            mutator.consume(preamble);
        }
    }

    @Override
    public void consume(SvnRevision revision) {
        for(SvnDumpMutator mutator : mutators) {
            mutator.consume(revision);
        }
    }

    @Override
    public void finish() {
        for(SvnDumpMutator mutator : mutators) {
            mutator.finish();
        }
    }
}
