package com.github.cstroe.svndumpgui.api;

import java.util.List;

public interface Repository {
    Preamble getPreamble();
    void setPreamble(Preamble preamble);
    List<Revision> getRevisions();
}
