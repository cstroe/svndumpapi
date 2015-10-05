package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnDumpWriterImplTest;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpSummary;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class SvnDumpSummaryTest {

    @Test
    public void summarize() throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        SvnDumpWriter summaryWriter = new SvnDumpSummary();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        summaryWriter.writeTo(baos);
        SvnDumpFileParserDoppelganger.consume(dump, summaryWriter);

        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("summary/svn_multi_file_delete.txt");

        SvnDumpWriterImplTest.assertEqualStreams(s, new ByteArrayInputStream(baos.toByteArray()));
    }

}