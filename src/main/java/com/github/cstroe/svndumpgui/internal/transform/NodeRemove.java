package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeRemove extends AbstractSvnDumpMutator {

    private final int targetRevision;
    private final String action;
    private final String path;

    private boolean foundTargetRevision = false;
    private boolean removedNode = false;
    private boolean inRemovedNode = false;

    public NodeRemove(int targetRevision, String action, String nodePath) {
        this.targetRevision = targetRevision;
        this.action = action;
        this.path = nodePath;
    }

    @Override
    public void consume(SvnRevision revision) {
        if(foundTargetRevision) {
            if(!removedNode) {
                throw new IllegalArgumentException("The node \"" + action + " " + path +
                        "\" was not found at revision " + targetRevision);
            }
        }

        if(revision.getNumber() == targetRevision) {
            foundTargetRevision = true;
        }

        super.consume(revision);
    }

    @Override
    public void consume(SvnNode node) {
        boolean nodeMatches = foundTargetRevision && !removedNode &&
                action.equals(node.get(SvnNodeHeader.ACTION)) &&
                path.equals(node.get(SvnNodeHeader.PATH));

        if(nodeMatches) {
            removedNode = true;
            inRemovedNode = true;
            return;
        }
        super.consume(node);
    }

    @Override
    public void consume(FileContentChunk chunk) {
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
    public void endNode(SvnNode node) {
        inRemovedNode = false;
        super.endNode(node);
    }

    @Override
    public void finish() {
        if(!foundTargetRevision) {
            throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
        }

        foundTargetRevision = false;
        removedNode = false;
        super.finish();
    }
}
