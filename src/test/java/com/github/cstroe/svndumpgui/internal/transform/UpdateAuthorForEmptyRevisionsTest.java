package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class UpdateAuthorForEmptyRevisionsTest {

    @Test
    public void update_empty_revisions() throws ParseException {
        SvnDumpMutator dumpMutator = new ClearRevision(3,4);
        SvnDump dump = SvnDumpFileParserDoppelganger.consume("dumps/add_edit_delete_add.dump", dumpMutator);

        assertThat(dump.getRevisions().size(), is(5));
        assertThat(dump.getRevisions().get(3).get(SvnProperty.AUTHOR), is("cosmin"));
        assertThat(dump.getRevisions().get(3).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(4).get(SvnProperty.AUTHOR), is("cosmin"));
        assertThat(dump.getRevisions().get(4).getNodes().size(), is(0));

        SvnDumpMutator updateAuthor = new UpdateAuthorForEmptyRevisions("blank");
        SvnDump updatedDump = SvnDumpFileParserDoppelganger.consume(dump, updateAuthor);

        assertThat(updatedDump.getRevisions().size(), is(5));
        assertThat(updatedDump.getRevisions().get(3).get(SvnProperty.AUTHOR), is("blank"));
        assertThat(updatedDump.getRevisions().get(3).getNodes().size(), is(0));
        assertThat(updatedDump.getRevisions().get(4).get(SvnProperty.AUTHOR), is("blank"));
        assertThat(updatedDump.getRevisions().get(4).getNodes().size(), is(0));
    }
}