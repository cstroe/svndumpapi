package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Revision;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

public class AbstractRepositoryConsumerTest {
    @Test
    public void chaining_should_work() {
        Mockery context = new Mockery();

        RepositoryConsumer mockConsumer = context.mock(RepositoryConsumer.class);

        Preamble mockPreamble = context.mock(Preamble.class);
        Revision mockRevision = context.mock(Revision.class);
        Node mockNode = context.mock(Node.class);
        ContentChunk mockChunk = context.mock(ContentChunk.class);

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


        AbstractRepositoryConsumer consumer = new AbstractRepositoryConsumer();
        consumer.continueTo(new AbstractRepositoryConsumer()); //  an intermediate consumer
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