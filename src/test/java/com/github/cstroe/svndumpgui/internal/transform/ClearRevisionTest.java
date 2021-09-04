package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.RepositoryMutator;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ClearRevisionTest {

    @Test
    public void clear_revision() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            Repository dumpBefore = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dumpBefore.getRevisions().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dumpBefore.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(2).getNodes().size(), is(3));
        }
        {
            RepositoryMutator cr = new ClearRevision(2);
            Repository dumpAfter = SvnDumpFileParserTest.consume(dumpFilePath, cr);

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
            Repository dumpBefore = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dumpBefore.getRevisions().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dumpBefore.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dumpBefore.getRevisions().get(2).getNodes().size(), is(3));
        }
        {
            RepositoryMutator clear = new ClearRevision(1, 2);
            Repository dumpAfter = SvnDumpFileParserTest.consume(dumpFilePath, clear);

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
    public void negative_revision_numbers_not_allowed_in_toRevision() {
        new ClearRevision(1,-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception_with_same_numbers() {
        new ClearRevision(1,1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void exception_with_descending_range() {
        new ClearRevision(2,1);
    }

    @Test
    public void consumer_chaining_works() throws ParseException {
        Mockery context = new Mockery();

        RepositoryConsumer mockConsumer = context.mock(RepositoryConsumer.class, "mockConsumer");

        Sequence consumerSequence = context.sequence("consumerSequence");

        final ClearRevision clearRevision = new ClearRevision(1,3);

        context.checking(new Expectations() {{
            // setup the consumer chain
            oneOf(mockConsumer).setPreviousConsumer(clearRevision); inSequence(consumerSequence);

            // preamble
            oneOf(mockConsumer).consume(with(any(Preamble.class))); inSequence(consumerSequence);

            // revision 0
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 1
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 2
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 3
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 4
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            oneOf(mockConsumer).finish();
        }});

        clearRevision.continueTo(mockConsumer);
        SvnDumpParser.consume(TestUtil.openResource("dumps/add_edit_delete_add.dump"), clearRevision);
    }

    @Test
    public void clear_already_empty_revision() throws ParseException {
        String theDumpWereWorkingWith = "dumps/add_edit_delete_add.dump";

        {
            RepositoryInMemory inMemoryDump = new RepositoryInMemory();
            SvnDumpParser.consume(TestUtil.openResource(theDumpWereWorkingWith), inMemoryDump);
            Repository initialDumpState = inMemoryDump.getRepo();

            assertThat(initialDumpState.getRevisions().size(), is(5));
            assertThat(initialDumpState.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(initialDumpState.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(initialDumpState.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(initialDumpState.getRevisions().get(3).getNodes().size(), is(1));
            assertThat(initialDumpState.getRevisions().get(4).getNodes().size(), is(1));
        }

        ByteArrayOutputStream clearedDumpStream = new ByteArrayOutputStream();
        {
            ClearRevision clearRevision = new ClearRevision(1, 3);
            RepositoryWriter writer = new SvnDumpWriter();
            writer.writeTo(clearedDumpStream);
            clearRevision.continueTo(writer);
            SvnDumpParser.consume(TestUtil.openResource(theDumpWereWorkingWith), clearRevision);
        }

        ClearRevision clearRevisionAgain = new ClearRevision(1,3);
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        clearRevisionAgain.continueTo(dumpInMemory);
        SvnDumpParser.consume(new ByteArrayInputStream(clearedDumpStream.toByteArray()), clearRevisionAgain);

        Repository dump = dumpInMemory.getRepo();
        assertThat(dump.getRevisions().size(), is(5));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(3).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(4).getNodes().size(), is(1));
    }
}
