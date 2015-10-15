package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ClearRevisionTest {

    @Test
    public void clear_revision() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            SvnDump dumpBefore = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dumpBefore.getRevisions().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dumpBefore.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(2).getNodes().size(), is(3));
        }
        {
            SvnDumpMutator cr = new ClearRevision(2);
            SvnDump dumpAfter = SvnDumpFileParserTest.consume(dumpFilePath, cr);

            assertThat(dumpAfter.getRevisions().size(), is(3));
            assertThat(dumpAfter.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dumpAfter.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dumpAfter.getRevisions().get(2).getNodes().size(), is(0)); // nodes cleared
        }
    }

    @Test
    public void clear_range() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            SvnDump dumpBefore = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dumpBefore.getRevisions().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dumpBefore.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(2).getNodes().size(), is(3));
        }
        {
            SvnDumpMutator clear = new ClearRevision(1, 2);
            SvnDump dumpAfter = SvnDumpFileParserTest.consume(dumpFilePath, clear);

            assertThat(dumpAfter.getRevisions().size(), is(3));
            assertThat(dumpAfter.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dumpAfter.getRevisions().get(1).getNodes().size(), is(0)); // nodes cleared
            assertThat(dumpAfter.getRevisions().get(2).getNodes().size(), is(0)); // nodes cleared
        }
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

    @Test
    public void consumer_chaining_works() throws ParseException {
        Mockery context = new Mockery();

        SvnDumpConsumer mockConsumer = context.mock(SvnDumpConsumer.class, "mockConsumer");

        Sequence consumerSequence = context.sequence("consumerSequence");

        context.checking(new Expectations() {{
            oneOf(mockConsumer).consume(with(any(SvnDumpPreamble.class))); inSequence(consumerSequence);

            // revision 0
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 1
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 2
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 3
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 4
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(FileContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            oneOf(mockConsumer).finish();
        }});

        ClearRevision clearRevision = new ClearRevision(1,3);
        clearRevision.continueTo(mockConsumer);
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/add_edit_delete_add.dump"), clearRevision);
    }
}