package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeRemove implements SvnDumpMutator {

    private final int targetRevision;
    private final String action;
    private final String path;

    private boolean foundTargetRevision = false;
    private boolean removedNode = false;

    public NodeRemove(int targetRevision, String action, String nodePath) {
        this.targetRevision = targetRevision;
        this.action = action;
        this.path = nodePath;
    }

    @Override
    public void mutateRevision(SvnRevision revision) {
        if(foundTargetRevision) {
            if(!removedNode) {
                throw new IllegalArgumentException("The node \"" + action + " " + path +
                        "\" was not found at revision " + targetRevision);
            }
            return;
        }

        if(revision.getNumber() == targetRevision) {
            foundTargetRevision = true;
            for(SvnNode node : revision.getNodes()) {
                if(foundTargetRevision && !removedNode &&
                        action.equals(node.get(SvnNodeHeader.ACTION)) &&
                        path.equals(node.get(SvnNodeHeader.PATH))) {
                    revision.getNodes().remove(node);
                    removedNode = true;
                    break;
                }
            }
        }
    }

    @Override
    public void finish() {
        if(!foundTargetRevision) {
            throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
        }

        foundTargetRevision = false;
        removedNode = false;
    }
}
