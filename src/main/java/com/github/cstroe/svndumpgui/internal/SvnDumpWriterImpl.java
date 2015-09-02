package com.github.cstroe.svndumpgui.internal;

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
        PrintStream ps = new PrintStream(getOutputStream());
        ps.println("SVN-fs-dump-format-version: 2\n");
        if(preamble.getUUID() != null) {
            ps.print("UUID: ");
            ps.println(preamble.getUUID());
        }
        ps.println();
    }

    @Override
    public void consume(SvnRevision revision) {
        PrintStream ps = new PrintStream(getOutputStream());
        ps.print("Revision-number: ");
        ps.println(revision.getNumber());

        // properties
        ByteArrayOutputStream properties = new ByteArrayOutputStream();
        writeProperties(new PrintStream(properties, true), revision.getProperties());
        int propertiesLength = properties.size();

        ps.print("Prop-content-length: ");
        ps.println(propertiesLength);
        ps.print("Content-length: ");
        ps.println(propertiesLength);
        ps.println();

        ps.print(properties.toString());
        ps.println();

        // nodes
        ByteArrayOutputStream nodes = new ByteArrayOutputStream();
        writeNodes(new PrintStream(nodes, true), revision);

        try {
            ps.write(nodes.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void finish() {}

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

    private void writeNodes(PrintStream ps, SvnRevision revision) {
        for(SvnNode node : revision.getNodes()) {
            // headers
            for(Map.Entry<SvnNodeHeader, String> headerEntry : node.getHeaders().entrySet()) {
                ps.print(headerEntry.getKey().toString());
                ps.println(headerEntry.getValue());
            }
            ps.println();

            // properties
            if(node.getHeaders().containsKey(SvnNodeHeader.PROP_CONTENT_LENGTH)) {
                writeProperties(ps, node.getProperties());
            }

            // file content
            if(node.getContent() != null && node.getContent().length != 0) {
                try {
                    ps.write(node.getContent());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
                ps.println();
            }

            // write an extra newline when there is no content and properties were written.
            if(node.getContent() == null && node.getHeaders().containsKey(SvnNodeHeader.PROP_CONTENT_LENGTH)) {
                ps.println();
            }

            ps.println();
        }
    }
}
