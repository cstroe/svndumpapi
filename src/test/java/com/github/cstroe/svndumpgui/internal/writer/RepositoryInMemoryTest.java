package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import java.io.InputStream;

public class RepositoryInMemoryTest {

    @Test
    public void consumer_chain_is_continued() throws ParseException {
        Mockery context = new Mockery();
        RepositoryConsumer chainedConsumer = context.mock(RepositoryConsumer.class, "chainedConsumer");
        final Sequence chainerConsumerSequence = context.sequence("chainedConsumerSequence");

        final RepositoryInMemory inMemory = new RepositoryInMemory();

        context.checking(new Expectations() {{
            // setup the consumer chain
            oneOf(chainedConsumer).setPreviousConsumer(inMemory); inSequence(chainerConsumerSequence);

            // preamble
            oneOf(chainedConsumer).consume(with(any(Preamble.class))); inSequence(chainerConsumerSequence);

            // revision 0
            oneOf(chainedConsumer).consume(with(any(Revision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(Revision.class))); inSequence(chainerConsumerSequence);

            // revision 1
            oneOf(chainedConsumer).consume(with(any(Revision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(ContentChunk.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endChunks(); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(Revision.class))); inSequence(chainerConsumerSequence);

            // revision 2
            oneOf(chainedConsumer).consume(with(any(Revision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(ContentChunk.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endChunks(); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(Revision.class))); inSequence(chainerConsumerSequence);

            // revision 3
            oneOf(chainedConsumer).consume(with(any(Revision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(Revision.class))); inSequence(chainerConsumerSequence);

            // revision 4
            oneOf(chainedConsumer).consume(with(any(Revision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(ContentChunk.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endChunks(); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(Node.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(Revision.class))); inSequence(chainerConsumerSequence);

            // finish
            oneOf(chainedConsumer).finish(); inSequence(chainerConsumerSequence);
        }});


        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dumps/add_edit_delete_add.dump");
        inMemory.continueTo(chainedConsumer);
        SvnDumpParser.consume(inputStream, inMemory);
    }
}