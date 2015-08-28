package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

public class SvnDumpWriterImpl implements SvnDumpWriter {
    @Override
    public void write(OutputStream os, SvnDump dump) {
        try(PrintStream ps = new PrintStream(os)) {
            writeDump(ps, dump);
        }
    }

    public void writeDump(PrintStream ps, SvnDump dump) {
        ps.println("SVN-fs-dump-format-version: 2\n");
        ps.print("UUID: ");
        ps.println(dump.getUUID());
        ps.println();

        for(SvnRevision revision : dump.getRevisions()) {
            writeRevision(ps, revision);
        }
    }

    public void writeRevision(PrintStream ps, SvnRevision revision) {
        ps.print("Revision-number: ");
        ps.println(revision.getNumber());

        // properties
        ByteArrayOutputStream properties = new ByteArrayOutputStream();
        writeProperties(new PrintStream(properties), revision);
        int propertiesLength = properties.size();

        ps.print("Prop-content-length: ");
        ps.println(propertiesLength);

        // nodes
        ByteArrayOutputStream nodes = new ByteArrayOutputStream();
        writeNodes(new PrintStream(nodes), revision);
        int contentLength = propertiesLength + nodes.size();

        ps.print("Content-length: ");
        ps.println(contentLength);
        ps.println();

        ps.print(properties.toString());
        ps.print(nodes.toString());
        ps.println();
    }

    public void writeProperties(PrintStream ps, SvnRevision revision) {
        for(String property : revision.getDefinedProperties()) {
            ps.print("K ");
            ps.println(property.length());
            ps.println(property);
            ps.print("V ");
            String propertyValue = revision.getProperty(property);
            ps.println(propertyValue.length());
            ps.println(propertyValue);
        }
        ps.println("PROPS-END");
    }

    private void writeNodes(PrintStream printStream, SvnRevision revision) {
    }
}
