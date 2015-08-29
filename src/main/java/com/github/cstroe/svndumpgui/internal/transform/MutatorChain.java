package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;

import java.util.ArrayList;
import java.util.List;

public class MutatorChain implements SvnDumpMutator {

    private List<SvnDumpMutator> mutators = new ArrayList<>();

    public void add(SvnDumpMutator mutator) {
        mutators.add(mutator);
    }

    @Override
    public void mutate(SvnDump dump) {
        for(SvnDumpMutator mutator : mutators) {
            mutator.mutate(dump);
        }
    }
}
