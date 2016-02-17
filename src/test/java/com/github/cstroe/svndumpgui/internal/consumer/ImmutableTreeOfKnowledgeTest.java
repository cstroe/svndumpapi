package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.api.TreeOfKnowledge;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

public class ImmutableTreeOfKnowledgeTest {
    private ImmutableTreeOfKnowledge itok;

    @Before
    public void setUp() {
        itok = new ImmutableTreeOfKnowledge(new TreeOfKnowledgeImpl());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setNextConsumer() {
        itok.setNextConsumer(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void setPreviousConsumer() {
        itok.setPreviousConsumer(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void consume_preamble() {
        itok.consume((Preamble)null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void consume_revision() {
        itok.consume((Revision)null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void consume_node() {
        itok.consume((Node)null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void consume_contentchunk() {
        itok.consume((ContentChunk)null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void endChunks() {
        itok.endChunks();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void endNode() {
        itok.endNode(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void endRevision() {
        itok.endRevision(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void finish() {
        itok.finish();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void continueTo() {
        itok.continueTo(null);
    }

    @Test
    public void tellMeAbout() {
        Mockery context = new Mockery();
        TreeOfKnowledge mockToK = context.mock(TreeOfKnowledge.class);

        final int revision = 1;
        final String path = "a/path";

        context.checking(new Expectations() {{
            oneOf(mockToK).tellMeAbout(revision, path);
        }});

        ImmutableTreeOfKnowledge itok = new ImmutableTreeOfKnowledge(mockToK);
        itok.tellMeAbout(revision, path);
    }
}