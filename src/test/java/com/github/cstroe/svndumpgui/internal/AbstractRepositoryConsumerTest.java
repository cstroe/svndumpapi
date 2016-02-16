package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Revision;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

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

        final AbstractRepositoryConsumer consumer = new MockAbstractRepositoryConsumer();

        context.checking(new Expectations() {{
            oneOf(mockConsumer).setPreviousConsumer(with(any(RepositoryConsumer.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockPreamble); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockRevision); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockNode); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(mockChunk); inSequence(consumerSequence);
            oneOf(mockConsumer).endChunks(); inSequence(consumerSequence);
            oneOf(mockConsumer).endNode(mockNode); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(mockRevision); inSequence(consumerSequence);
            oneOf(mockConsumer).finish(); inSequence(consumerSequence);
        }});


        consumer.continueTo(new MockAbstractRepositoryConsumer()); //  an intermediate consumer
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

    @Test
    public void previous_consumer_set_when_setting_next_consumer() {
        RepositoryConsumer consumer1 = new MockAbstractRepositoryConsumer();
        RepositoryConsumer consumer2 = new MockAbstractRepositoryConsumer();

        consumer1.continueTo(consumer2);
        assertThat(consumer2.getPreviousConsumer(), is(consumer1));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void continueTo_should_only_allow_operations_on_the_head_of_the_chain() {
        RepositoryConsumer headConsumer = new MockAbstractRepositoryConsumer();
        RepositoryConsumer consumer2 = new MockAbstractRepositoryConsumer();
        RepositoryConsumer consumer3 = new MockAbstractRepositoryConsumer();

        headConsumer.continueTo(consumer2);
        consumer2.continueTo(consumer3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void cant_continueTo_yourself() {
        RepositoryConsumer consumer = new MockAbstractRepositoryConsumer();
        consumer.continueTo(consumer);
    }

    @Test(expected = IllegalStateException.class)
    public void avoid_cycles() {
        RepositoryConsumer consumer1 = new MockAbstractRepositoryConsumer();
        RepositoryConsumer consumer2 = new MockAbstractRepositoryConsumer();
        RepositoryConsumer consumer3 = new MockAbstractRepositoryConsumer();

        consumer1.continueTo(consumer2);
        consumer1.continueTo(consumer3);
        consumer1.continueTo(consumer2);
    }

    public static class MockAbstractRepositoryConsumer extends AbstractRepositoryConsumer {}
}