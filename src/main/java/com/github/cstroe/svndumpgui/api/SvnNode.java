package com.github.cstroe.svndumpgui.api;

import java.util.Map;
import java.util.Optional;

public interface SvnNode {
    /**
     * @return An SvnNode can be "detached", i.e., not belong to a revision.
     */
    Optional<SvnRevision> getRevision();
    void setRevision(SvnRevision revision);

    Map<SvnNodeHeader, String> getHeaders();
    void setHeaders(Map<SvnNodeHeader, String> headers);

    Map<String, String> getProperties();
    void setProperties(Map<String, String> properties);

    byte[] getContent();
    void setContent(byte[] content);

    // utility method
    String get(SvnNodeHeader header);
}
