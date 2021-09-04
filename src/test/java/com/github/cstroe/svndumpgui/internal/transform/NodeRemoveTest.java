package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.RepositoryMutator;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNull;

public class NodeRemoveTest {

    @Test
    public void remove_node() throws ParseException {
        Repository dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        Node node = dump.getRevisions().get(2).getNodes().get(1);
        assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
        assertNull(node.get(NodeHeader.KIND));
        assertThat(node.get(NodeHeader.PATH), is(equalTo("README2.txt")));

        RepositoryMutator nodeRemove = new NodeRemove(2, "delete", "README2.txt");
        Repository updatedDump = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", nodeRemove);

        assertThat(updatedDump.getRevisions().size(), is(3));
        assertThat(updatedDump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(updatedDump.getRevisions().get(2).getNodes().size(), is(2)); // node cleared
        Node firstNode = updatedDump.getRevisions().get(2).getNodes().get(0);
        Node secondNode = updatedDump.getRevisions().get(2).getNodes().get(1);

        assertThat(firstNode.get(NodeHeader.PATH), is(not(equalTo("README2.txt"))));
        assertThat(secondNode.get(NodeHeader.PATH), is(not(equalTo("README2.txt"))));
    }

    @Test
    public void remove_should_respect_the_target_revision_number() throws ParseException {
        final String dumpFilePath = "dumps/add_edit_delete_add.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(5));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            Node node = dump.getRevisions().get(1).getNodes().get(0);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README.txt")));

            assertThat(dump.getRevisions().get(4).getNodes().size(), is(1));
            node = dump.getRevisions().get(4).getNodes().get(0);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README.txt")));
        }
        {
            RepositoryMutator nodeRemove = new NodeRemove(4, "add", "README.txt");
            Repository updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, nodeRemove);

            assertThat(updatedDump.getRevisions().size(), is(5));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(1));
            Node nodeAfter = updatedDump.getRevisions().get(1).getNodes().get(0);
            assertThat(nodeAfter.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(nodeAfter.get(NodeHeader.KIND), is(equalTo("file")));
            assertThat(nodeAfter.get(NodeHeader.PATH), is(equalTo("README.txt")));
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

        RepositoryConsumer mockConsumer = context.mock(RepositoryConsumer.class, "mockConsumer");

        context.checking(new Expectations() {{
            // setup the consumer chain
            oneOf(mockConsumer).setPreviousConsumer(with(any(NodeRemove.class))); inSequence(consumerSequence);

            // preamble
            oneOf(mockConsumer).consume(with(any(Preamble.class))); inSequence(consumerSequence);

            // revision 0
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 1
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 2
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            oneOf(mockConsumer).finish(); inSequence(consumerSequence);
        }});

        NodeRemove nr = new NodeRemove(1, "add", "README.txt");
        nr.continueTo(new NodeRemove(2, "delete", "README.txt"));
        nr.continueTo(mockConsumer);

        SvnDumpParser.consume(TestUtil.openResource("dumps/svn_multi_file_delete.dump"), nr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_when_node_was_not_found() {
        NodeRemove nr = new NodeRemove(1, "add", "README.txt");
        nr.finish();
    }
}