package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnDumpAuthorsTest {

    @Test
    public void read_authors() throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete_multiple_authors.dump");
        SvnDumpWriter authorsWriter = new SvnDumpAuthors();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        authorsWriter.write(baos, dump);

        assertThat(baos.toString(), is(equalTo("super2\nsuperd\n")));
    }

    @Test
    public void filter_duplicates() throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");
        SvnDumpWriter authorsWriter = new SvnDumpAuthors();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        authorsWriter.write(baos, dump);

        assertThat(baos.toString(), is(equalTo("cosmin\n")));
    }
}