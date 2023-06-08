package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeRemoveByPath extends AbstractRepositoryMutator {
    private final Pattern pathPattern;
    private final boolean invert;

    private boolean inRemovedNode = false;

    public NodeRemoveByPath(Pattern nodePathPattern) {
        this(nodePathPattern, false);
    }

    public NodeRemoveByPath(Pattern nodePathPattern, boolean invert) {
        this.pathPattern = nodePathPattern;
        this.invert = invert;
    }

    @Override
    public void consume(Node node) {
        String nodePath = node.getPath().get();
        Matcher matcher = pathPattern.matcher(nodePath);
        boolean nodeMatches = matcher.find();

        if (invert) {
            nodeMatches = !nodeMatches;
        }

        if(nodeMatches) {
            inRemovedNode = true;
            return;
        }
        super.consume(node);
    }

    @Override
    public void consume(ContentChunk chunk) {
        if(!inRemovedNode) {
            super.consume(chunk);
        }
    }

    @Override
    public void endChunks() {
        if(!inRemovedNode) {
            super.endChunks();
        }
    }

    @Override
    public void endNode(Node node) {
        if(inRemovedNode) {
            inRemovedNode = false;
        } else {
            super.endNode(node);
        }
    }
}
