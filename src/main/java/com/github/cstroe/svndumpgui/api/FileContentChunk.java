package com.github.cstroe.svndumpgui.api;

public interface FileContentChunk {
    byte[] getContent();
    void setContent(byte[] content);
}
