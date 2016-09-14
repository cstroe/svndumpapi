package com.github.cstroe.svndumpgui.api;

public interface RepositoryFilter {
    Preamble consume(Preamble preamble);
    Revision consume(Revision revision);
    Revision endRevision(Revision revision);
    Node consume(Node node);
    Node endNode(Node node);
    ContentChunk consume(ContentChunk chunk);
    Object endChunks();
    Object finish();
}
