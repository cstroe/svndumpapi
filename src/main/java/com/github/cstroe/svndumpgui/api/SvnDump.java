package com.github.cstroe.svndumpgui.api;

import java.util.List;

public interface SvnDump {
    void addRevision(SvnRevision revision);
    List<SvnRevision> getRevisions();
}
