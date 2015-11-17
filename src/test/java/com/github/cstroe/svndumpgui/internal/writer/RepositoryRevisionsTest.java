package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpParserDoppelganger;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class RepositoryRevisionsTest {
    @Test
    public void print_revisions() throws ParseException, IOException {
        Repository dump = SvnDumpParserDoppelganger.parse("dumps/add_edit_delete_add.dump");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        RepositoryWriter printRevisions = new RepositoryRevisions();
        printRevisions.writeTo(out);
        SvnDumpParserDoppelganger.consume(dump, printRevisions);

        assertThat(out.toString(), is(equalTo("Finished revision 0.\nFinished revision 1.\nFinished revision 2.\nFinished revision 3.\nFinished revision 4.\n")));
    }
}