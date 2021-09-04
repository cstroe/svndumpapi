package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.RepositoryMutator;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeAddTest {
    @Test
    public void simple_add() throws ParseException {
        final Map<NodeHeader, String> headers;
        {
            Map<NodeHeader, String> map = new LinkedHashMap<>();
            map.put(NodeHeader.ACTION, "add");
            map.put(NodeHeader.KIND, "dir");
            map.put(NodeHeader.PATH, "testdir");
            headers = Collections.unmodifiableMap(map);
        }

        String dumpFilePath = "dumps/firstcommit.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(2));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        }
        {
            Node newNode = new NodeImpl();
            newNode.setHeaders(headers);

            RepositoryMutator nodeAdd = new NodeAdd(1, newNode);
            Repository updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, nodeAdd);

            assertThat(updatedDump.getRevisions().size(), is(2));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(2));

            Node addedNode = updatedDump.getRevisions().get(1).getNodes().get(0);
            assertThat(addedNode.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(addedNode.get(NodeHeader.KIND), is(equalTo("dir")));
            assertThat(addedNode.get(NodeHeader.PATH), is(equalTo("testdir")));
        }
    }

    @Test
    public void consumer_chaining_and_adding_node_works() throws ParseException, NoSuchAlgorithmException {
        Node nodeToAdd;
        {
            StringWriter stringWriter = new StringWriter();
            stringWriter.append("This is some file content.");
            byte[] fileContent = stringWriter.toString().getBytes();

            NodeImpl newFileNode = new NodeImpl();
            newFileNode.addFileContentChunk(new ContentChunkImpl(fileContent));
            newFileNode.getHeaders().put(NodeHeader.ACTION, "add");
            newFileNode.getHeaders().put(NodeHeader.KIND, "file");
            newFileNode.getHeaders().put(NodeHeader.PATH, "FILE.txt");
            newFileNode.getHeaders().put(NodeHeader.TEXT_CONTENT_LENGTH, Integer.toString(fileContent.length));
            newFileNode.getHeaders().put(NodeHeader.CONTENT_LENGTH, Integer.toString(fileContent.length));
            newFileNode.getHeaders().put(NodeHeader.MD5, TestUtil.md5sum(fileContent));
            nodeToAdd = newFileNode;
        }

        Mockery context = new Mockery();

        RepositoryConsumer mockConsumer = context.mock(RepositoryConsumer.class, "mockConsumer");

        Sequence consumerSequence = context.sequence("consumerSequence");

        final NodeAdd nodeAdd = new NodeAdd(3, nodeToAdd);

        context.checking(new Expectations() {{
            // setup the consumer chain
            oneOf(mockConsumer).setPreviousConsumer(nodeAdd); inSequence(consumerSequence);

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
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 2
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);

            // revision 3
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);

            // this node is added
            oneOf(mockConsumer).consume(with(nodeToAdd)); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(nodeToAdd.getContent().get(0))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(nodeToAdd)); inSequence(consumerSequence);

            // this node was there before
            oneOf(mockConsumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(Node.class))); inSequence(consumerSequence);

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

        nodeAdd.continueTo(mockConsumer);
        SvnDumpParser.consume(TestUtil.openResource("dumps/add_edit_delete_add.dump"), nodeAdd);

    }

    @Test(expected = IllegalArgumentException.class)
    public void throw_exception_when_node_not_added() throws ParseException, NoSuchAlgorithmException {
        Node nodeToAdd;
        {
            StringWriter stringWriter = new StringWriter();
            stringWriter.append("This is some file content.");
            byte[] fileContent = stringWriter.toString().getBytes();

            NodeImpl newFileNode = new NodeImpl();
            newFileNode.addFileContentChunk(new ContentChunkImpl(fileContent));
            newFileNode.getHeaders().put(NodeHeader.ACTION, "add");
            newFileNode.getHeaders().put(NodeHeader.KIND, "file");
            newFileNode.getHeaders().put(NodeHeader.PATH, "FILE.txt");
            newFileNode.getHeaders().put(NodeHeader.TEXT_CONTENT_LENGTH, Integer.toString(fileContent.length));
            newFileNode.getHeaders().put(NodeHeader.CONTENT_LENGTH, Integer.toString(fileContent.length));
            newFileNode.getHeaders().put(NodeHeader.MD5, TestUtil.md5sum(fileContent));
            nodeToAdd = newFileNode;
        }

        NodeAdd nodeAdd = new NodeAdd(6, nodeToAdd);
        SvnDumpParser.consume(TestUtil.openResource("dumps/add_edit_delete_add.dump"), nodeAdd);
    }
}