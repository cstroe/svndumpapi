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
import com.github.cstroe.svndumpgui.internal.FileContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
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
import static org.junit.Assert.*;

public class NodeAddTest {
    @Test
    public void simple_add() throws ParseException {
        final Map<SvnNodeHeader, String> headers;
        {
            Map<SvnNodeHeader, String> map = new LinkedHashMap<>();
            map.put(SvnNodeHeader.ACTION, "add");
            map.put(SvnNodeHeader.KIND, "dir");
            map.put(SvnNodeHeader.PATH, "testdir");
            headers = Collections.unmodifiableMap(map);
        }

        String dumpFilePath = "dumps/firstcommit.dump";
        {
            SvnDump dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(2));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        }
        {
            SvnNode newNode = new SvnNodeImpl();
            newNode.setHeaders(headers);

            SvnDumpMutator nodeAdd = new NodeAdd(1, newNode);
            SvnDump updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, nodeAdd);

            assertThat(updatedDump.getRevisions().size(), is(2));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(2));

            SvnNode addedNode = updatedDump.getRevisions().get(1).getNodes().get(0);
            assertThat(addedNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
            assertThat(addedNode.get(SvnNodeHeader.KIND), is(equalTo("dir")));
            assertThat(addedNode.get(SvnNodeHeader.PATH), is(equalTo("testdir")));
        }
    }

    @Test
    public void consumer_chaining_and_adding_node_works() throws ParseException, NoSuchAlgorithmException {
        SvnNode nodeToAdd;
        {
            StringWriter stringWriter = new StringWriter();
            stringWriter.append("This is some file content.");
            byte[] fileContent = stringWriter.toString().getBytes();

            SvnNodeImpl newFileNode = new SvnNodeImpl();
            newFileNode.addFileContentChunk(new FileContentChunkImpl(fileContent));
            newFileNode.getHeaders().put(SvnNodeHeader.ACTION, "add");
            newFileNode.getHeaders().put(SvnNodeHeader.KIND, "file");
            newFileNode.getHeaders().put(SvnNodeHeader.PATH, "FILE.txt");
            newFileNode.getHeaders().put(SvnNodeHeader.TEXT_CONTENT_LENGTH, Integer.toString(fileContent.length));
            newFileNode.getHeaders().put(SvnNodeHeader.CONTENT_LENGTH, Integer.toString(fileContent.length));
            newFileNode.getHeaders().put(SvnNodeHeader.MD5, TestUtil.md5sum(fileContent));
            nodeToAdd = newFileNode;
        }

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
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(FileContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 2
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(FileContentChunk.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // revision 3
            oneOf(mockConsumer).consume(with(any(SvnRevision.class))); inSequence(consumerSequence);

            // this node is added
            oneOf(mockConsumer).consume(with(nodeToAdd)); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(nodeToAdd.getContent().get(0))); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(nodeToAdd)); inSequence(consumerSequence);

            // this node was there before
            oneOf(mockConsumer).consume(with(any(SvnNode.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(with(any(SvnNode.class))); inSequence(consumerSequence);

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


        NodeAdd nodeAdd = new NodeAdd(3, nodeToAdd);
        nodeAdd.continueTo(mockConsumer);
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/add_edit_delete_add.dump"), nodeAdd);

    }
}