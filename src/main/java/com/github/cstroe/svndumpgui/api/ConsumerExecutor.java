package com.github.cstroe.svndumpgui.api;

public class ConsumerExecutor implements RepositoryConsumer {
    private final RepositoryConsumer consumerChain;

    public ConsumerExecutor(RepositoryConsumer consumerChain) {
        this.consumerChain = consumerChain;
    }

    @Override
    public void consume(Preamble preamble) {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.consume(preamble);
        }
    }

    @Override
    public void consume(Revision revision) {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.consume(revision);
        }

    }

    @Override
    public void endRevision(Revision revision) {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.endRevision(revision);
        }
    }

    @Override
    public void consume(Node node) {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.consume(node);
        }
    }

    @Override
    public void endNode(Node node) {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.endNode(node);
        }
    }

    @Override
    public void consume(ContentChunk chunk) {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.consume(chunk);
        }
    }

    @Override
    public void endChunks() {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.endChunks();
        }
    }

    @Override
    public void finish() {
        for(RepositoryConsumer currentConsumer = consumerChain;
            currentConsumer != null;
            currentConsumer = currentConsumer.getNextConsumer()) {
            currentConsumer.finish();
        }
    }

    @Override
    public RepositoryConsumer getNextConsumer() {
        return null;
    }

    @Override
    public void setNextConsumer(RepositoryConsumer consumer) {
        consumer.continueTo(consumer);
    }

    @Override
    public RepositoryConsumer getPreviousConsumer() {
        return null;
    }

    @Override
    public void setPreviousConsumer(RepositoryConsumer consumer) {
        throw new UnsupportedOperationException("This is the executor!");
    }
}
