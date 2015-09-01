package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

public class SvnDumpWriterImpl implements SvnDumpWriter {

    @Override
    public void writePreamble(OutputStream os, SvnDump dump) throws IOException {
        writeDump(new PrintStream(os), dump);
    }

    @Override
    public void writeRevision(OutputStream os, SvnRevision revision) throws IOException {
        writeRevision(new PrintStream(os), revision);
    }

    @Override
    public void finish(OutputStream os) {}

    private void writeDump(PrintStream ps, SvnDump dump) throws IOException {
        ps.println("SVN-fs-dump-format-version: 2\n");
        ps.print("UUID: ");
        ps.println(dump.getUUID());
        ps.println();
    }

    private void writeRevision(PrintStream ps, SvnRevision revision) throws IOException {
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

        ps.write(nodes.toByteArray());
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

    private void writeNodes(PrintStream ps, SvnRevision revision) throws IOException {
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
                ps.write(node.getContent());
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
