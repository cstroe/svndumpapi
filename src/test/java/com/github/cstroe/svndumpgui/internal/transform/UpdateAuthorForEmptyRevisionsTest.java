package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryMutator;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpParserDoppelganger;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class UpdateAuthorForEmptyRevisionsTest {

    @Test
    public void update_empty_revisions() throws ParseException {
        RepositoryMutator dumpMutator = new ClearRevision(3,4);
        Repository dump = SvnDumpParserDoppelganger.consume("dumps/add_edit_delete_add.dump", dumpMutator);

        assertThat(dump.getRevisions().size(), is(5));
        assertThat(dump.getRevisions().get(3).get(Property.AUTHOR), is("cosmin"));
        assertThat(dump.getRevisions().get(3).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(4).get(Property.AUTHOR), is("cosmin"));
        assertThat(dump.getRevisions().get(4).getNodes().size(), is(0));

        RepositoryMutator updateAuthor = new UpdateAuthorForEmptyRevisions("blank");
        Repository updatedDump = SvnDumpParserDoppelganger.consume(dump, updateAuthor);

        assertThat(updatedDump.getRevisions().size(), is(5));
        assertThat(updatedDump.getRevisions().get(3).get(Property.AUTHOR), is("blank"));
        assertThat(updatedDump.getRevisions().get(3).getNodes().size(), is(0));
        assertThat(updatedDump.getRevisions().get(4).get(Property.AUTHOR), is("blank"));
        assertThat(updatedDump.getRevisions().get(4).getNodes().size(), is(0));
    }
}