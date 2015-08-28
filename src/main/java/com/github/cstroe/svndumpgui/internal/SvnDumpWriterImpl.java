package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;

public class SvnDumpWriterImpl implements SvnDumpWriter {
    @Override
    public void write(OutputStream os, SvnDump dump) throws IOException {
        try(PrintStream ps = new PrintStream(os)) {
            writeDump(ps, dump);
        }
    }

    public void writeDump(PrintStream ps, SvnDump dump) throws IOException {
        ps.println("SVN-fs-dump-format-version: 2\n");
        ps.print("UUID: ");
        ps.println(dump.getUUID());
        ps.println();

        for(SvnRevision revision : dump.getRevisions()) {
            writeRevision(ps, revision);
        }
    }

    public void writeRevision(PrintStream ps, SvnRevision revision) throws IOException {
        ps.print("Revision-number: ");
        ps.println(revision.getNumber());

        // properties
        ByteArrayOutputStream properties = new ByteArrayOutputStream();
        writeProperties(new PrintStream(properties), revision.getProperties());
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
        writeNodes(new PrintStream(nodes), revision);

        ps.print(nodes.toString());
    }

    public void writeProperties(PrintStream ps, Map<String, String> properties) {
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
            ps.print("Node-path: ");
            ps.println(node.getPath());
            ps.print("Node-kind: ");
            ps.println(node.getKind());
            ps.print("Node-action: ");
            ps.println(node.getAction());

            // properties
            ByteArrayOutputStream properties = new ByteArrayOutputStream();
            writeProperties(new PrintStream(properties), node.getProperties());
            int propertiesLength = properties.size();

            ps.print("Prop-content-length: ");
            ps.println(propertiesLength);

            ps.print("Text-content-length: ");
            int textContentLength = 0;
            if(node.getContent() != null) {
                textContentLength = node.getContent().length;
            }
            ps.println(textContentLength);

            if(node.getMd5() != null) {
                ps.print("Text-content-md5: ");
                ps.println(node.getMd5());
            }

            if(node.getSha1() != null) {
                ps.print("Text-content-sha1: ");
                ps.println(node.getSha1());
            }

            ps.print("Content-length: ");
            ps.println(propertiesLength + textContentLength);
            ps.println();

            ps.print(properties.toString());

            if(node.getContent() != null && node.getContent().length != 0) {
                ps.write(node.getContent());
                ps.println();
            }

            ps.println();
        }
    }
}
