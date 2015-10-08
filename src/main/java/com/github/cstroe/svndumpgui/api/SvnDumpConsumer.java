package com.github.cstroe.svndumpgui.api;

public interface SvnDumpConsumer {
    void consume(SvnDumpPreamble preamble);
    void consume(SvnRevision revision);
    void endRevision(SvnRevision revision);
    void consume(SvnNode node);
    void endNode(SvnNode node);
    void consume(FileContentChunk chunk);
    void endChunks();
    void finish();

    SvnDumpConsumer getNextConsumer();
    void setNextConsumer(SvnDumpConsumer consumer);

    /**
     * This enables "chained consumers".  A consumer will inherently
     * know how to pass information on to the next consumer.
     *
     * This allows multiple consumers to operate on a stream
     * in one pass, instead of requiring a pass for each consumer.
     *
     * @param nextConsumer the consumer that should process
     *                     the stream after the last consumer in this chain.
     */
    default void continueTo(SvnDumpConsumer nextConsumer) {
        SvnDumpConsumer lastConsumer = this;
        while(lastConsumer.getNextConsumer() != null) {
            lastConsumer = lastConsumer.getNextConsumer();
        }
        lastConsumer.setNextConsumer(nextConsumer);
    }
}
