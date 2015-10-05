package com.github.cstroe.svndumpgui.api;

public class FileContentChunk {
    private byte[] content;

    public FileContentChunk(byte[] content) {
        this.content = content;
    }

    public FileContentChunk(FileContentChunk chunk) {
        byte[] chunkContent = chunk.getContent();
        if(chunkContent != null) {
            byte[] copy = new byte[chunkContent.length];
            System.arraycopy(chunkContent, 0, copy, 0, chunkContent.length);
            this.content = copy;
        }
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
