package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.PreambleImpl;
import com.github.cstroe.svndumpgui.internal.RepositoryImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

public class SvnDumpParserDoppelgangerTest {

    @Test
    public void consume_should_work() {
        final RepositoryImpl dump = new RepositoryImpl();
        PreambleImpl preamble = new PreambleImpl();
        dump.setPreamble(preamble);
        Revision r0 = new RevisionImpl(0);
        dump.addRevision(r0);

        Revision r1 = new RevisionImpl(1);
        dump.addRevision(r1);
        NodeImpl n1_1 = new NodeImpl(r1);
        {
            n1_1.getHeaders().put(NodeHeader.ACTION, "add");
            n1_1.getHeaders().put(NodeHeader.KIND, "file");
            n1_1.getHeaders().put(NodeHeader.PATH, "test1");
            r1.addNode(n1_1);
        }

        Revision r2 = new RevisionImpl(2);
        dump.addRevision(r2);
        NodeImpl n2_1 = new NodeImpl(r2);
        final ContentChunk c1;
        final ContentChunk c2;
        NodeImpl n2_2 = new NodeImpl(r2);
        {
            n2_1.getHeaders().put(NodeHeader.ACTION, "add");
            n2_1.getHeaders().put(NodeHeader.KIND, "file");
            n2_1.getHeaders().put(NodeHeader.PATH, "test2");
            r2.addNode(n2_1);
            byte[] bytes1 = new byte[2];
            bytes1[0] = 'a';
            bytes1[1] = 't';
            byte[] bytes2 = new byte[3];
            bytes2[0] = 'o';
            bytes2[1] = 'n';
            bytes2[2] = 'e';
            c1 = new ContentChunkImpl(bytes1);
            n2_1.addFileContentChunk(c1);
            c2 = new ContentChunkImpl(bytes2);
            n2_1.addFileContentChunk(c2);

            n2_2.getHeaders().put(NodeHeader.ACTION, "add");
            n2_2.getHeaders().put(NodeHeader.KIND, "file");
            n2_2.getHeaders().put(NodeHeader.PATH, "test3");
            r2.addNode(n2_2);
        }

        Mockery context = new Mockery();

        RepositoryConsumer consumer = context.mock(RepositoryConsumer.class, "c1");

        final Sequence consumerSequence = context.sequence("consumerSequence");
        context.checking(new Expectations() {{
            oneOf(consumer).consume(preamble); inSequence(consumerSequence);
            oneOf(consumer).consume(r0); inSequence(consumerSequence);
            oneOf(consumer).endRevision(r0); inSequence(consumerSequence);
            oneOf(consumer).consume(r1); inSequence(consumerSequence);
            oneOf(consumer).consume(n1_1); inSequence(consumerSequence);
            oneOf(consumer).endNode(n1_1); inSequence(consumerSequence);
            oneOf(consumer).endRevision(r1); inSequence(consumerSequence);
            oneOf(consumer).consume(r2); inSequence(consumerSequence);
            oneOf(consumer).consume(n2_1); inSequence(consumerSequence);
            oneOf(consumer).consume(c1); inSequence(consumerSequence);
            oneOf(consumer).consume(c2); inSequence(consumerSequence);
            oneOf(consumer).endChunks(); inSequence(consumerSequence);
            oneOf(consumer).endNode(n2_1); inSequence(consumerSequence);
            oneOf(consumer).consume(n2_2); inSequence(consumerSequence);
            oneOf(consumer).endNode(n2_2); inSequence(consumerSequence);
            oneOf(consumer).endRevision(r2); inSequence(consumerSequence);
            oneOf(consumer).finish(); inSequence(consumerSequence);
        }});

        SvnDumpParserDoppelganger.consumeWithoutChaining(dump, consumer);
    }

    @Test
    public void consume_file_content() {
        final RepositoryImpl dump = new RepositoryImpl();
        PreambleImpl preamble = new PreambleImpl();
        dump.setPreamble(preamble);

        Revision r0 = new RevisionImpl(0);
        dump.addRevision(r0);

        Revision r1 = new RevisionImpl(1);
        dump.addRevision(r1);
        NodeImpl n1_1 = new NodeImpl(r1);
        {
            r1.addNode(n1_1);
        }
    }
}