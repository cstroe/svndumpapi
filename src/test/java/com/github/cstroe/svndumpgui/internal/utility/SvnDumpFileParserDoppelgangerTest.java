package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpPreambleImpl;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Test;

public class SvnDumpFileParserDoppelgangerTest {

    @Test
    public void consume_should_work() {
        final SvnDumpImpl dump = new SvnDumpImpl();
        SvnDumpPreambleImpl preamble = new SvnDumpPreambleImpl();
        dump.setPreamble(preamble);
        SvnRevision r0 = new SvnRevisionImpl(0);
        dump.addRevision(r0);

        SvnRevision r1 = new SvnRevisionImpl(1);
        dump.addRevision(r1);
        SvnNodeImpl n1_1 = new SvnNodeImpl(r1);
        {
            r1.addNode(n1_1);
        }

        SvnRevision r2 = new SvnRevisionImpl(2);
        dump.addRevision(r2);
        SvnNodeImpl n2_1 = new SvnNodeImpl(r2);
        SvnNodeImpl n2_2 = new SvnNodeImpl(r2);
        {
            r2.addNode(n2_1);
            r2.addNode(n2_2);
        }

        Mockery context = new Mockery();

        SvnDumpConsumer consumer = context.mock(SvnDumpConsumer.class, "c1");

        final Sequence consumerSequence = context.sequence("consumerSequence");
        context.checking(new Expectations() {{
            oneOf(consumer).consume(preamble); inSequence(consumerSequence);
            oneOf(consumer).consume(r0); inSequence(consumerSequence);
            oneOf(consumer).consume(r1); inSequence(consumerSequence);
            oneOf(consumer).consume(n1_1); inSequence(consumerSequence);
            oneOf(consumer).consume(r2); inSequence(consumerSequence);
            oneOf(consumer).consume(n2_1); inSequence(consumerSequence);
            oneOf(consumer).consume(n2_2); inSequence(consumerSequence);
            oneOf(consumer).finish(); inSequence(consumerSequence);
        }});

        SvnDumpFileParserDoppelganger.consumeWithoutChaining(dump, consumer);
    }
}