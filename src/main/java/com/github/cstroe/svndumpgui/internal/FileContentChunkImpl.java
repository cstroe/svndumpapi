package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.FileContentChunk;

public class FileContentChunkImpl implements FileContentChunk  {
    private byte[] content;

    public FileContentChunkImpl(byte[] content) {
        if(content == null) {
            throw new IllegalArgumentException("A FileContentChunk must have some content.");
        }
        this.content = content;
    }

    public FileContentChunkImpl(FileContentChunk chunk) {
        byte[] chunkContent = chunk.getContent();
        if(chunkContent != null) {
            byte[] copy = new byte[chunkContent.length];
            System.arraycopy(chunkContent, 0, copy, 0, chunkContent.length);
            this.content = copy;
        } else {
            throw new IllegalArgumentException("A FileContentChunk must have some content.");
        }
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void setContent(byte[] content) {
        if(content == null) {
            throw new IllegalArgumentException("A FileContentChunk must have some content.");
        }
        this.content = content;
    }

    @Override
    public String toString() {
        return Integer.toString(content.length) + " bytes";
    }
}
