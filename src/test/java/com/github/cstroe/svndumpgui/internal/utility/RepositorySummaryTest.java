package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.RepositoryImpl;
import com.github.cstroe.svndumpgui.internal.PreambleImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.cstroe.svndumpgui.internal.utility.TestUtil.assertEqualStreams;

public class RepositorySummaryTest {

    @Test
    public void summarize_in_memory() throws ParseException, IOException {
        Repository dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        RepositoryWriter summaryWriter = new RepositorySummary();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        summaryWriter.writeTo(baos);
        SvnDumpParserDoppelganger.consume(dump, summaryWriter);

        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/svn_multi_file_delete.txt");

        assertEqualStreams(s, new ByteArrayInputStream(baos.toByteArray()));
    }

    @Test
    public void summarize_from_stream() throws ParseException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RepositorySummary svnDumpSummary = new RepositorySummary();
        svnDumpSummary.writeTo(baos);

        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/svn_multi_file_delete.dump");
        SvnDumpParser.consume(inputStream, svnDumpSummary);

        InputStream summaryStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/svn_multi_file_delete.txt");

        assertEqualStreams(summaryStream, new ByteArrayInputStream(baos.toByteArray()));
    }

    @Test
    public void multiple_empty_revisions() throws IOException, ParseException {
        RepositoryImpl dump = new RepositoryImpl();
        PreambleImpl preamble = new PreambleImpl("903a69a2-8256-45e6-a9dc-d9a846114b23");
        dump.setPreamble(preamble);
        Revision r0 = new RevisionImpl(0);
        dump.addRevision(r0);
        Revision r1 = new RevisionImpl(1);
        dump.addRevision(r1);
        Revision r2 = new RevisionImpl(2);
        dump.addRevision(r2);
        Revision r3 = new RevisionImpl(3);
        dump.addRevision(r3);
        r3.getProperties().put(Property.AUTHOR, "cosmin");
        r3.getProperties().put(Property.LOG, "a log message");
        r3.getProperties().put(Property.DATE, "2015-08-28T03:38:50.644836Z");

        NodeImpl n3_1 = new NodeImpl(r3);
        r3.addNode(n3_1);
        n3_1.getHeaders().put(NodeHeader.ACTION, "add");
        n3_1.getHeaders().put(NodeHeader.KIND, "dir");
        n3_1.getHeaders().put(NodeHeader.PATH, "directory1");

        SvnDumpWriter writer = new SvnDumpWriter();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.writeTo(outputStream);
        SvnDumpParserDoppelganger.consumeWithoutChaining(dump, writer);

        RepositorySummary dumpSummary = new RepositorySummary();
        ByteArrayOutputStream summaryStream = new ByteArrayOutputStream();
        dumpSummary.writeTo(summaryStream);
        SvnDumpParser.consume(new ByteArrayInputStream(outputStream.toByteArray()), dumpSummary);

        InputStream expectedStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/empty_revisions_at_beginning.txt");

        assertEqualStreams(expectedStream, new ByteArrayInputStream(summaryStream.toByteArray()));
    }
}