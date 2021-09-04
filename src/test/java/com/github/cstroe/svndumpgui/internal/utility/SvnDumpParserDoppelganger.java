package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;

import java.io.InputStream;

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
public class SvnDumpParserDoppelganger {
    private final Repository dump;

    public SvnDumpParserDoppelganger(Repository dump) {
        this.dump = dump;
    }

    public void Start(RepositoryConsumer consumer) {
        consumer.consume(dump.getPreamble());
        for(Revision revision : dump.getRevisions()) {
            consumer.consume(revision);
            for(Node node : revision.getNodes()) {
                consumer.consume(node);
                boolean wroteChunk = false;
                for(ContentChunk chunk : node.getContent()) {
                    wroteChunk = true;
                    consumer.consume(chunk);
                }
                if(wroteChunk) {
                    consumer.endChunks();
                }
                consumer.endNode(node);
            }
            consumer.endRevision(revision);
        }
        consumer.finish();
    }

    public static Repository parse(String fileName) throws ParseException {
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        consume(fileName, dumpInMemory);
        return dumpInMemory.getRepo();
    }

    public static Repository consume(Repository dump, RepositoryConsumer consumer) {
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        consumer.continueTo(dumpInMemory);
        new SvnDumpParserDoppelganger(dump).Start(consumer);
        return dumpInMemory.getRepo();
    }

    public static Repository consume(String fileName, RepositoryConsumer consumer) throws ParseException {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(fileName);

        return consume(is, consumer);
    }

    public static Repository consume(InputStream is, RepositoryConsumer consumer) throws ParseException {
        RepositoryInMemory svnDumpInMemory = new RepositoryInMemory();
        consumer.continueTo(svnDumpInMemory);
        SvnDumpParser.consume(is, consumer);
        return svnDumpInMemory.getRepo();
    }

    /**
     * Same as {@link #consume(com.github.cstroe.svndumpgui.api.Repository, com.github.cstroe.svndumpgui.api.RepositoryConsumer)}
     * but without adding another consumer to the chain.  Because we don't add another
     * consumer, we have nothing to return.
     */
    public static void consumeWithoutChaining(Repository dump, RepositoryConsumer consumer) {
        new SvnDumpParserDoppelganger(dump).Start(consumer);
    }
}
