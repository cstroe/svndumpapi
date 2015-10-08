package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
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

}