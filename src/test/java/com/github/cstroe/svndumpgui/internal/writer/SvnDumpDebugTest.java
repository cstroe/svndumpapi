package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class SvnDumpDebugTest {
    @Test
    public void debugging_messages_work() throws ParseException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpDebug debug = new SvnDumpDebug(baos);
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/svn_multi_file_delete_multiple_authors.dump"), debug);

        TestUtil.assertEqualStreams(TestUtil.openResource("debug/debugging_messages_work.txt"),
                new ByteArrayInputStream(baos.toByteArray()));
    }

}