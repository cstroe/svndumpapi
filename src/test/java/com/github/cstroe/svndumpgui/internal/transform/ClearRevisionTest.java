package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ClearRevisionTest {

    @Test
    public void clear_revision() throws ParseException {
        SvnDumpMutator cr = new ClearRevision(3);
        SvnDump dump = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", cr);
        
        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));

        SvnDumpMutator clear = new ClearRevision(2);
        clear.mutate(dump);

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(0)); // nodes cleared
    }

    @Test
    public void clear_range() throws ParseException {
        SvnDump dumpBefore = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dumpBefore.getRevisions().size(), is(3));
        assertThat(dumpBefore.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dumpBefore.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dumpBefore.getRevisions().get(2).getNodes().size(), is(3));

        SvnDumpMutator clear = new ClearRevision(1,2);
        SvnDump dumpAfter = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", clear);

        assertThat(dumpAfter.getRevisions().size(), is(3));
        assertThat(dumpAfter.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dumpAfter.getRevisions().get(1).getNodes().size(), is(0)); // nodes cleared
        assertThat(dumpAfter.getRevisions().get(2).getNodes().size(), is(0)); // nodes cleared
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_revision_numbers_not_allowed() {
        new ClearRevision(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_revision_numbers_not_allowed_again() {
        new ClearRevision(-1,1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception_with_same_numbers() {
        new ClearRevision(1,1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception_with_descending_range() {
        new ClearRevision(2,1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cant_change_non_existent_revision() throws ParseException {
        SvnDumpConsumer cr = new ClearRevision(3);
        SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", cr);
    }
}