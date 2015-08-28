package com.github.cstroe.svndumpgui.api;

import java.util.List;

public interface SvnDump {
    String getUUID();
    List<SvnRevision> getRevisions();
}
