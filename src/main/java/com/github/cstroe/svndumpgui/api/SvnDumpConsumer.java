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

    /**
     * This enables "chained consumers".  A consumer will inherently
     * know how to pass information on to the next consumer.
     *
     * This allows multiple consumers to operate on a stream
     * in one pass, instead of requiring a pass for each consumer.
     *
     * @param nextConsumer the consumer that should process
     *                     the stream after this consumer.
     */
    void continueTo(SvnDumpConsumer nextConsumer);

    SvnDumpConsumer getNextConsumer();

    /**
     * A fluent-style method to create the consumer chain.
     *
     * Example usage:
     *
     * <pre>
     *     SvnDumpConsumer firstConsumer = new SvnDumpConsumer();
     *     SvnDumpConsumer secondConsumer = new SvnDumpConsumer().after(firstConsumer);
     * </pre>
     *
     * @param previousSvnDumpConsumer the SvnDumpConsumer before the current one.
     * @return the current SvnDumpConsumer
     */
    default SvnDumpConsumer after(SvnDumpConsumer previousSvnDumpConsumer) {
        previousSvnDumpConsumer.continueTo(this);
        return this;
    }

    /**
     * A fluent-style method to get the last consumer in the current chain.
     *
     * Example usage:
     *
     * <pre>
     *     SvnDumpConsumer head = new SvnDumpConsumer();
     *     head.tail().continueTo(new SvnDumpConsumer());
     *     head.tail().continueTo(new SvnDumpConsumer());
     *     ...
     * </pre>
     *
     * @return The tail end of the current consumer chain.
     */
    default SvnDumpConsumer tail() {
        SvnDumpConsumer currentConsumer = this;
        while(this.getNextConsumer() != null) {
            currentConsumer = this.getNextConsumer();
        }
        return currentConsumer;
    }
}
