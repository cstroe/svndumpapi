package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.SimplePrintStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

public class RepositoryWriterImpl extends AbstractRepositoryWriter {

    @Override
    public void consume(Preamble preamble) {
        ps().println("SVN-fs-dump-format-version: 2\n");
        if(preamble.getUUID() != null) {
            ps().print("UUID: ");
            ps().println(preamble.getUUID());
        }
        ps().println();

        super.consume(preamble);
    }

    @Override
    public void consume(Revision revision) {
        ps().print("Revision-number: ");
        ps().println(revision.getNumber());

        // properties are created here so that we can fill in the header values correctly
        ByteArrayOutputStream properties = new ByteArrayOutputStream();
        writeProperties(new SimplePrintStream(properties), revision.getProperties());
        int propertiesLength = properties.size();

        ps().print("Prop-content-length: ");
        ps().println(propertiesLength);
        ps().print("Content-length: ");
        ps().println(propertiesLength);
        ps().println();

        ps().print(properties.toString());
        ps().println();

        super.consume(revision);
    }

    private void writeProperties(SimplePrintStream ps, Map<String, String> properties) {
        if(properties == null) {
            return;
        }
        for(Map.Entry<String, String> entry : properties.entrySet()) {
            ps.print("K ");
            ps.println(entry.getKey().getBytes().length);
            ps.println(entry.getKey());
            ps.print("V ");
            ps.println(entry.getValue().getBytes().length);
            ps.println(entry.getValue());
        }
        ps.println("PROPS-END");
        ps.flush();
    }

    @Override
    public void consume(Node node) {
        // headers
        for(Map.Entry<NodeHeader, String> headerEntry : node.getHeaders().entrySet()) {
            ps().print(headerEntry.getKey().toString());
            ps().println(headerEntry.getValue());
        }
        ps().println();

        // properties
        if(node.getHeaders().containsKey(NodeHeader.PROP_CONTENT_LENGTH)) {
            writeProperties(ps(), node.getProperties());

            // write an extra newline when there is no content and properties were written.
            if(node.get(NodeHeader.TEXT_CONTENT_LENGTH) == null || Long.parseLong(node.get(NodeHeader.TEXT_CONTENT_LENGTH)) == 0) {
                ps().println();
            }
        }

        super.consume(node);
    }

    @Override
    public void endNode(Node node) {
        if(node.getProperties().containsKey(Property.TRAILING_NEWLINE_HINT)) {
            int numNewlines = Integer.parseInt(node.getProperties().get(Property.TRAILING_NEWLINE_HINT));
            for(int i = 1; i < numNewlines; i++) {
                ps().println();
            }
        } else {
            ps().println();
        }
        super.endNode(node);
    }

    @Override
    public void consume(ContentChunk chunk) {
        if(chunk == null) {
            throw new IllegalArgumentException("Cannot accept null chunks.");
        } else if(chunk.getContent() == null) {
            throw new IllegalArgumentException("Cannot accept chunks with null content.");
        }

        try {
            ps().write(chunk.getContent());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        super.consume(chunk);
    }

    @Override
    public void endChunks() {
        ps().println();
        super.endChunks();
    }
}
