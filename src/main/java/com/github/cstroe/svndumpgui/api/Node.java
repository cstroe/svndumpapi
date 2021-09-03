package com.github.cstroe.svndumpgui.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface Node {
    /**
     * @return A node can be "detached", i.e., not belong to a revision.
     */
    Optional<Revision> getRevision();
    void setRevision(Revision revision);

    Map<NodeHeader, String> getHeaders();
    void setHeaders(Map<NodeHeader, String> headers);

    Map<String, String> getProperties();
    void setProperties(Map<String, String> properties);

    List<ContentChunk> getContent();
    void addFileContentChunk(ContentChunk chunk);

    /**
     * @return the value of a header defined in this node
     */
    String get(NodeHeader header);

    /**
     * Utility method to return a byte array for the entire file content.  This
     * will concatenate all the byte arrays of each ContentChunk.
     */
    default byte[] getByteContent() {
        int totalLength = getContent().stream().mapToInt(c -> c.getContent().length).sum();
        byte[] bigByte = new byte[totalLength];

        int currentPosition = 0;
        for(ContentChunk chunk : getContent()) {
            int chunkLength = chunk.getContent().length;
            System.arraycopy(chunk.getContent(), 0, bigByte, currentPosition, chunkLength);
            currentPosition += chunkLength;
        }

        return bigByte;
    }
}
