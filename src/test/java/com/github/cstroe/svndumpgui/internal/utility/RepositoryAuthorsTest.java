package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryAuthors;
import org.junit.Test;

import java.io.ByteArrayOutputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class RepositoryAuthorsTest {

    @Test
    public void read_authors() throws ParseException {
        Repository dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete_multiple_authors.dump");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RepositoryWriter authorsWriter = new RepositoryAuthors();
        authorsWriter.writeTo(baos);
        SvnDumpParserDoppelganger.consume(dump, authorsWriter);

        assertThat(baos.toString(), is(equalTo("superd\nsuper2\n")));
    }

    @Test
    public void filter_duplicates() throws ParseException {
        Repository dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");
        RepositoryWriter authorsWriter = new RepositoryAuthors();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        authorsWriter.writeTo(baos);
        SvnDumpParserDoppelganger.consume(dump, authorsWriter);

        assertThat(baos.toString(), is(equalTo("cosmin\n")));
    }
}