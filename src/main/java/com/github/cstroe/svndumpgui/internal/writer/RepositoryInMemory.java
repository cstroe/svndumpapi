package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.PreambleImpl;
import com.github.cstroe.svndumpgui.internal.RepositoryImpl;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;

public class RepositoryInMemory extends AbstractRepositoryWriter {

    private Repository dump;
    private Revision currentRevision;
    private Node currentNode;

    @Override
    public void consume(Preamble preamble) {
        dump = new RepositoryImpl();
        dump.setPreamble(new PreambleImpl(preamble));
        super.consume(preamble);
    }

    @Override
    public void consume(Revision revision) {
        currentRevision = new RevisionImpl(revision);
        dump.getRevisions().add(currentRevision);
        super.consume(revision);
    }

    @Override
    public void endRevision(Revision revision) {
        currentRevision = null;
        super.endRevision(revision);
    }

    @Override
    public void consume(Node node) {
        currentNode = new NodeImpl(node);
        currentNode.setRevision(currentRevision);
        currentRevision.addNode(currentNode);
        super.consume(node);
    }

    @Override
    public void endNode(Node node) {
        currentNode = null;
        super.endNode(node);
    }

    @Override
    public void consume(ContentChunk chunk) {
        currentNode.addFileContentChunk(chunk);
        super.consume(chunk);
    }

    public Repository getRepo() {
        return dump;
    }
}
