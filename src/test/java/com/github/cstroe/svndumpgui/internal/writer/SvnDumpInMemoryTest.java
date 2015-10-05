package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

import java.io.InputStream;

public class SvnDumpInMemoryTest {

    @Test
    public void consumer_chain_is_continued() throws ParseException {
        Mockery context = new Mockery();
        SvnDumpConsumer chainedConsumer = context.mock(SvnDumpConsumer.class, "chainedConsumer");
        final Sequence chainerConsumerSequence = context.sequence("chainedConsumerSequence");

        context.checking(new Expectations() {{
            // preamble
            oneOf(chainedConsumer).consume(with(any(SvnDumpPreamble.class))); inSequence(chainerConsumerSequence);

            // revision 0
            oneOf(chainedConsumer).consume(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);

            // revision 1
            oneOf(chainedConsumer).consume(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(FileContentChunk.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endChunks(); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);

            // revision 2
            oneOf(chainedConsumer).consume(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(FileContentChunk.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endChunks(); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);

            // revision 3
            oneOf(chainedConsumer).consume(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);

            // revision 4
            oneOf(chainedConsumer).consume(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).consume(with(any(FileContentChunk.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endChunks(); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endNode(with(any(SvnNode.class))); inSequence(chainerConsumerSequence);
            oneOf(chainedConsumer).endRevision(with(any(SvnRevision.class))); inSequence(chainerConsumerSequence);

            // finish
            oneOf(chainedConsumer).finish(); inSequence(chainerConsumerSequence);
        }});


        final InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("dumps/add_edit_delete_add.dump");
        SvnDumpInMemory inMemory = new SvnDumpInMemory();
        inMemory.continueTo(chainedConsumer);
        SvnDumpFileParser.consume(inputStream, inMemory);
    }
}