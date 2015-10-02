package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpInMemory;

/**
 * Convenience class to mimic what happens in the SvnDumpFileParser, but
 * instead of reading from a Reader like SvnDumpFileParser, we process an
 * SvnDump that's already parsed in memory.
 *
 * No parsing happens here, this just duplicates the "consumption" mechanism
 * that's encoded in SvnDumpFileParser.
 *
 * Useful in tests.
 */
public class SvnDumpFileParserDoppelganger {
    private SvnDump dump;

    public SvnDumpFileParserDoppelganger(SvnDump dump) {
        this.dump = dump;
    }

    public void Start(SvnDumpConsumer consumer) {
        consumer.consume(dump.getPreamble());
        for(SvnRevision revision : dump.getRevisions()) {
            consumer.consume(revision);
            for(SvnNode node : revision.getNodes()) {
                consumer.consume(node);
            }
        }
        consumer.finish();
    }

    public static SvnDump consume(SvnDump dump, SvnDumpConsumer consumer) {
        SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
        consumer.continueTo(dumpInMemory);
        new SvnDumpFileParserDoppelganger(dump).Start(consumer);
        return dumpInMemory.getDump();
    }

    /**
     * Same as {@link #consume(com.github.cstroe.svndumpgui.api.SvnDump, com.github.cstroe.svndumpgui.api.SvnDumpConsumer)}
     * but without adding another consumer to the chain.  Because we don't add another
     * consumer, we have nothing to return.
     */
    public static void consumeWithoutChaining(SvnDump dump, SvnDumpConsumer consumer) {
        new SvnDumpFileParserDoppelganger(dump).Start(consumer);
    }
}
