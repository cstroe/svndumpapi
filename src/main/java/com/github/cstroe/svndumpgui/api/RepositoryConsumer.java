package com.github.cstroe.svndumpgui.api;

public interface RepositoryConsumer {
    void consume(Preamble preamble);
    void consume(Revision revision);
    void endRevision(Revision revision);
    void consume(Node node);
    void endNode(Node node);
    void consume(ContentChunk chunk);
    void endChunks();
    void finish();

    RepositoryConsumer getNextConsumer();
    void setNextConsumer(RepositoryConsumer consumer);
    RepositoryConsumer getPreviousConsumer();
    void setPreviousConsumer(RepositoryConsumer consumer);

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
    default void continueTo(RepositoryConsumer nextConsumer) {
        RepositoryConsumer lastConsumer = this;
        while(lastConsumer.getNextConsumer() != null) {
            lastConsumer = lastConsumer.getNextConsumer();
        }
        lastConsumer.setNextConsumer(nextConsumer);
        nextConsumer.setPreviousConsumer(lastConsumer);
    }
}
