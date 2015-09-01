package com.github.cstroe.svndumpgui.api;

import java.util.Iterator;
import java.util.List;

public interface SvnDump {
    String getUUID();
    List<SvnRevision> getRevisions();
    Iterator<SvnRevision> revisions();
}
