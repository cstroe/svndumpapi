package com.github.cstroe.svndumpgui.api;

import java.util.List;

public interface SvnDump {
    SvnDumpPreamble getPreamble();
    void setPreamble(SvnDumpPreamble preamble);
    List<SvnRevision> getRevisions();
}
