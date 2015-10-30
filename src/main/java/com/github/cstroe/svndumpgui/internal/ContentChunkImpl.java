package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.ContentChunk;

public class ContentChunkImpl implements ContentChunk {
    private byte[] content;

    public ContentChunkImpl(byte[] content) {
        if(content == null) {
            throw new IllegalArgumentException("A ContentChunk must have some content.");
        }
        this.content = content;
    }

    public ContentChunkImpl(ContentChunk chunk) {
        byte[] chunkContent = chunk.getContent();
        if(chunkContent == null) {
            throw new IllegalArgumentException("A ContentChunk must have some content.");
        }

        byte[] copy = new byte[chunkContent.length];
        System.arraycopy(chunkContent, 0, copy, 0, chunkContent.length);
        this.content = copy;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    @Override
    public void setContent(byte[] content) {
        if(content == null) {
            throw new IllegalArgumentException("A ContentChunk must have some content.");
        }
        this.content = content;
    }

    @Override
    public String toString() {
        return Integer.toString(content.length) + " bytes";
    }
}
