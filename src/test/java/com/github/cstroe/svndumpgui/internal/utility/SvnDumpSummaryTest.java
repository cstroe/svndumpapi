package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpPreambleImpl;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImpl;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImplTest;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpSummary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SvnDumpSummaryTest {

    @Test
    public void summarize_in_memory() throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        SvnDumpWriter summaryWriter = new SvnDumpSummary();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        summaryWriter.writeTo(baos);
        SvnDumpFileParserDoppelganger.consume(dump, summaryWriter);

        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/svn_multi_file_delete.txt");

        SvnDumpWriterImplTest.assertEqualStreams(s, new ByteArrayInputStream(baos.toByteArray()));
    }

    @Test
    public void summarize_from_stream() throws ParseException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpSummary svnDumpSummary = new SvnDumpSummary();
        svnDumpSummary.writeTo(baos);

        final InputStream inputStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/svn_multi_file_delete.dump");
        SvnDumpFileParser.consume(inputStream, svnDumpSummary);

        InputStream summaryStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/svn_multi_file_delete.txt");

        SvnDumpWriterImplTest.assertEqualStreams(summaryStream, new ByteArrayInputStream(baos.toByteArray()));
    }

    @Test
    public void multiple_empty_revisions() throws IOException, ParseException {
        SvnDumpImpl dump = new SvnDumpImpl();
        SvnDumpPreambleImpl preamble = new SvnDumpPreambleImpl("903a69a2-8256-45e6-a9dc-d9a846114b23");
        dump.setPreamble(preamble);
        SvnRevision r0 = new SvnRevisionImpl(0);
        dump.addRevision(r0);
        SvnRevision r1 = new SvnRevisionImpl(1);
        dump.addRevision(r1);
        SvnRevision r2 = new SvnRevisionImpl(2);
        dump.addRevision(r2);
        SvnRevision r3 = new SvnRevisionImpl(3);
        dump.addRevision(r3);
        r3.getProperties().put(SvnProperty.AUTHOR, "cosmin");
        r3.getProperties().put(SvnProperty.LOG, "a log message");
        r3.getProperties().put(SvnProperty.DATE, "2015-08-28T03:38:50.644836Z");

        SvnNodeImpl n3_1 = new SvnNodeImpl(r3);
        r3.addNode(n3_1);
        n3_1.getHeaders().put(SvnNodeHeader.ACTION, "add");
        n3_1.getHeaders().put(SvnNodeHeader.KIND, "dir");
        n3_1.getHeaders().put(SvnNodeHeader.PATH, "directory1");

        SvnDumpWriterImpl writer = new SvnDumpWriterImpl();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.writeTo(outputStream);
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(dump, writer);

        SvnDumpSummary dumpSummary = new SvnDumpSummary();
        ByteArrayOutputStream summaryStream = new ByteArrayOutputStream();
        dumpSummary.writeTo(summaryStream);
        SvnDumpFileParser.consume(new ByteArrayInputStream(outputStream.toByteArray()), dumpSummary);

        InputStream expectedStream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/empty_revisions_at_beginning.txt");

        SvnDumpWriterImplTest.assertEqualStreams(expectedStream, new ByteArrayInputStream(summaryStream.toByteArray()));
    }
}