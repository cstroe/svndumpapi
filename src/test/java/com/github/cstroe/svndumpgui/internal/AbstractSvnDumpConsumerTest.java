package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

public class AbstractSvnDumpConsumerTest {
    @Test
    public void chaining_should_work() {
        Mockery context = new Mockery();

        SvnDumpConsumer mockConsumer = context.mock(SvnDumpConsumer.class);

        SvnDumpPreamble mockPreamble = context.mock(SvnDumpPreamble.class);
        SvnRevision mockRevision = context.mock(SvnRevision.class);
        SvnNode mockNode = context.mock(SvnNode.class);
        FileContentChunk mockChunk = context.mock(FileContentChunk.class);

        Sequence consumerSequence = context.sequence("consumerSequence");

        context.checking(new Expectations() {{
            oneOf(mockConsumer).consume(mockPreamble); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockRevision); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockNode); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockChunk); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(mockNode); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(mockRevision); inSequence(consumerSequence);
            oneOf(mockConsumer).finish(); inSequence(consumerSequence);
        }});


        AbstractSvnDumpConsumer consumer = new AbstractSvnDumpConsumer();
        consumer.continueTo(new AbstractSvnDumpConsumer()); //  an intermediate consumer
        consumer.continueTo(mockConsumer);

        consumer.consume(mockPreamble);
        consumer.consume(mockRevision);
        consumer.consume(mockNode);
        consumer.consume(mockChunk);
        consumer.endChunks();
        consumer.endNode(mockNode);
        consumer.endRevision(mockRevision);
        consumer.finish();
    }
}