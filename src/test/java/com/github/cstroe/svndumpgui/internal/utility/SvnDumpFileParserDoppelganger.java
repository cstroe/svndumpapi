package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpInMemory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

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

    public static SvnDump parse(String fileName) throws ParseException {
        SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
        consume(fileName, dumpInMemory);
        return dumpInMemory.getDump();
    }

    public static SvnDump consume(SvnDump dump, SvnDumpConsumer consumer) {
        SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
        consumer.continueTo(dumpInMemory);
        new SvnDumpFileParserDoppelganger(dump).Start(consumer);
        return dumpInMemory.getDump();
    }

    public static SvnDump consume(String fileName, SvnDumpConsumer consumer) throws ParseException {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileName);

        Reader reader;
        try {
            reader = new InputStreamReader(is, "ISO-8859-1");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getMessage());
        }

        SvnDumpInMemory svnDumpInMemory = new SvnDumpInMemory();
        consumer.continueTo(svnDumpInMemory);
        SvnDumpFileParser.consume(reader, consumer);
        return svnDumpInMemory.getDump();
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
