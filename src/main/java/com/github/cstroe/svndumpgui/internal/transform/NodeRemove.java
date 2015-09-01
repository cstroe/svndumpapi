package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.*;

public class NodeRemove extends AbstractSvnDumpMutator {

    private final int targetRevision;
    private final String action;
    private final String path;

    private SvnRevision currentRevision = null;
    private boolean foundTargetRevision;
    private boolean removedNode;

    public NodeRemove(int targetRevision, String action, String nodePath) {
        this.targetRevision = targetRevision;
        this.action = action;
        this.path = nodePath;
    }

    @Override
    public void mutate(SvnRevision revision) {
        if(foundTargetRevision) {
            if(!removedNode) {
                throw new IllegalArgumentException("The node \"" + action + " " + path +
                        "\" was not found at revision " + targetRevision);
            }
            return;
        }

        if(revision.getNumber() == targetRevision) {
            currentRevision = revision;
            foundTargetRevision = true;
        }
    }

    @Override
    public void mutate(SvnNode node) {
        if(!removedNode &&
                action.equals(node.get(SvnNodeHeader.ACTION)) &&
                path.equals(node.get(SvnNodeHeader.PATH))) {
            currentRevision.getNodes().remove(node);
        }
    }

    @Override
    public void finish() {
        if(!foundTargetRevision) {
            throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
        }

        currentRevision = null;
        foundTargetRevision = false;
        removedNode = false;
    }
}
