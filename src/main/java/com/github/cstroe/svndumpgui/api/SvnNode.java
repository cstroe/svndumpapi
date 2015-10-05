package com.github.cstroe.svndumpgui.api;

import java.util.List;
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

    List<FileContentChunk> getContent();
    void addFileContentChunk(FileContentChunk chunk);

    // utility method
    String get(SvnNodeHeader header);

    /**
     * Utility method to return a byte array for the entire file content.  This
     * will concatenate all the byte arrays of each FileContentChunk.
     */
    default byte[] getByteContent() {
        int totalLength = getContent().stream().mapToInt(c -> c.getContent().length).sum();
        byte[] bigByte = new byte[totalLength];

        int currentPosition = 0;
        for(FileContentChunk chunk : getContent()) {
            int chunkLength = chunk.getContent().length;
            System.arraycopy(chunk.getContent(), 0, bigByte, currentPosition, chunkLength);
            currentPosition += chunkLength;
        }

        return bigByte;
    }
}
