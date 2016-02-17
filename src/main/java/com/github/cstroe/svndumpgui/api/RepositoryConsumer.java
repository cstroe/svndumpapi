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
        if(this == nextConsumer) {
            throw new IllegalArgumentException("Cannot continue to yourself.");
        }

        if(this.getPreviousConsumer() != null) {
            throw new UnsupportedOperationException("Must continue from the head of the chain.");
        }

        RepositoryConsumer lastConsumer = this;
        while(lastConsumer.getNextConsumer() != null) {
            RepositoryConsumer currentConsumer = lastConsumer.getNextConsumer();
            if(currentConsumer == nextConsumer) {
                throw new IllegalStateException("Cannot add a duplicate consumer.");
            }
            lastConsumer = currentConsumer;
        }
        lastConsumer.setNextConsumer(nextConsumer);
        nextConsumer.setPreviousConsumer(lastConsumer);
    }
}
