package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnDumpRevisionsTest {
    @Test
    public void print_revisions() throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserDoppelganger.parse("dumps/add_edit_delete_add.dump");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SvnDumpWriter printRevisions = new SvnDumpRevisions();
        SvnDumpFileParserDoppelganger.consume(dump, printRevisions);

        assertThat(out.toString(), is(equalTo("Finished revision 0.\nFinished revision 1.\nFinished revision 2.\nFinished revision 3.\nFinished revision 4.\n")));
    }
}