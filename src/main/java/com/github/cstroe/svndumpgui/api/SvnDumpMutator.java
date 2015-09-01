package com.github.cstroe.svndumpgui.api;

public interface SvnDumpMutator {
    void mutate(SvnDump dump);
    void mutate(SvnRevision revision);
    void finish();
}
