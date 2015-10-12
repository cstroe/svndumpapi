package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class NodeRemoveTest {

    @Test
    public void remove_node() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        SvnNode node = dump.getRevisions().get(2).getNodes().get(1);
        assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
        assertNull(node.get(SvnNodeHeader.KIND));
        assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README2.txt")));

        SvnDumpMutator nodeRemove = new NodeRemove(2, "delete", "README2.txt");
        SvnDump updatedDump = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", nodeRemove);

        assertThat(updatedDump.getRevisions().size(), is(3));
        assertThat(updatedDump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(updatedDump.getRevisions().get(2).getNodes().size(), is(2)); // node cleared
        SvnNode firstNode = updatedDump.getRevisions().get(2).getNodes().get(0);
        SvnNode secondNode = updatedDump.getRevisions().get(2).getNodes().get(1);

        assertThat(firstNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));
        assertThat(secondNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));
    }

    @Test
    public void remove_should_respect_the_target_revision_number() throws ParseException {
        final String dumpFilePath = "dumps/add_edit_delete_add.dump";
        {
            SvnDump dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(5));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            SvnNode node = dump.getRevisions().get(1).getNodes().get(0);
            assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("add")));
            assertThat(node.get(SvnNodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));

            assertThat(dump.getRevisions().get(4).getNodes().size(), is(1));
            node = dump.getRevisions().get(4).getNodes().get(0);
            assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("add")));
            assertThat(node.get(SvnNodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        }
        {
            SvnDumpMutator nodeRemove = new NodeRemove(4, "add", "README.txt");
            SvnDump updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, nodeRemove);

            assertThat(updatedDump.getRevisions().size(), is(5));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(1));
            SvnNode nodeAfter = updatedDump.getRevisions().get(1).getNodes().get(0);
            assertThat(nodeAfter.get(SvnNodeHeader.ACTION), is(equalTo("add")));
            assertThat(nodeAfter.get(SvnNodeHeader.KIND), is(equalTo("file")));
            assertThat(nodeAfter.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
            assertThat(updatedDump.getRevisions().get(4).getNodes().size(), is(0));
        }
    }

    @Test
    public void should_accept_valid_actions() {
        new NodeRemove(1, "add", "path/to/file.txt");
        new NodeRemove(1, "delete", "path/to/file.txt");
        new NodeRemove(1, "change", "path/to/file.txt");
        new NodeRemove(1, "replace", "path/to/file.txt");
    }

    @Test(expected = RuntimeException.class)
    public void should_reject_invalid_action() {
        new NodeRemove(1, "someaction", "path/to/file.txt");
    }

    @Test
    public void chain_should_not_continue_for_removed_nodes() throws ParseException {
        Mockery context = new Mockery();
        Sequence consumerSequence = context.sequence("consumerSequence");

        SvnDumpConsumer mockConsumer = context.mock(SvnDumpConsumer.class, "mockConsumer");

        context.checking(new Expectations() {{
            oneOf(mockConsumer).consume(with(any(SvnDumpPreamble.class))); inSequence(consumerSequence);

            // revision 0
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 1
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(FileContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(FileContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 2
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            oneOf(mockConsumer).finish(); inSequence(consumerSequence);
        }});

        NodeRemove nr = new NodeRemove(1, "add", "README.txt");
        nr.continueTo(new NodeRemove(2, "delete", "README.txt"));
        nr.continueTo(mockConsumer);

        SvnDumpFileParser.consume(TestUtil.openResource("dumps/svn_multi_file_delete.dump"), nr);
    }
}