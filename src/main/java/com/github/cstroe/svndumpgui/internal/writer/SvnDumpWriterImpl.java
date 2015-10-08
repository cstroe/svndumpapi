package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.writer.AbstractSvnDumpWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class SvnDumpWriterImpl extends AbstractSvnDumpWriter {

    @Override
    public void consume(SvnDumpPreamble preamble) {
        ps().println("SVN-fs-dump-format-version: 2\n");
        if(preamble.getUUID() != null) {
            ps().print("UUID: ");
            ps().println(preamble.getUUID());
        }
        ps().println();

        super.consume(preamble);
    }

    @Override
    public void consume(SvnRevision revision) {
        ps().print("Revision-number: ");
        ps().println(revision.getNumber());

        // properties are created here so that we can fill in the header values correctly
        ByteArrayOutputStream properties = new ByteArrayOutputStream();
        writeProperties(new PrintStream(properties, true), revision.getProperties());
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

    private void writeProperties(PrintStream ps, Map<String, String> properties) {
        if(properties == null) {
            return;
        }
        for(Map.Entry<String, String> entry : properties.entrySet()) {
            ps.print("K ");
            ps.println(entry.getKey().length());
            ps.println(entry.getKey());
            ps.print("V ");
            ps.println(entry.getValue().length());
            ps.println(entry.getValue());
        }
        ps.println("PROPS-END");
    }

    @Override
    public void consume(SvnNode node) {
        // headers
        for(Map.Entry<SvnNodeHeader, String> headerEntry : node.getHeaders().entrySet()) {
            ps().print(headerEntry.getKey().toString());
            ps().println(headerEntry.getValue());
        }
        ps().println();

        // properties
        if(node.getHeaders().containsKey(SvnNodeHeader.PROP_CONTENT_LENGTH)) {
            writeProperties(ps(), node.getProperties());

            // write an extra newline when there is no content and properties were written.
            if(node.get(SvnNodeHeader.TEXT_CONTENT_LENGTH) == null) {
                ps().println();
            }
        }

        super.consume(node);
    }

    @Override
    public void endNode(SvnNode node) {
        ps().println();
        super.endNode(node);
    }

    @Override
    public void consume(FileContentChunk chunk) {
        try {
            if(chunk.getContent() != null && chunk.getContent().length > 0) {
                ps().write(chunk.getContent());
            }
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
