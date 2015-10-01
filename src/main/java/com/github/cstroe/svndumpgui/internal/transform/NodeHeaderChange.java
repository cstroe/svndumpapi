package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeHeaderChange extends AbstractSvnDumpMutator {

    private final int targetRevision;
    private final String nodeAction;
    private final String nodePath;
    private final SvnNodeHeader headerToChange;
    private final String oldValue;
    private final String newValue;

    private boolean foundTargetRevision;
    private boolean updatedNode;

    public NodeHeaderChange(int targetRevision, String nodeAction, String path, SvnNodeHeader headerToChange, String oldValue, String newValue) {
        this.targetRevision = targetRevision;
        this.nodeAction = nodeAction;
        this.nodePath = path;
        this.headerToChange = headerToChange;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void consume(SvnRevision revision) {
        if(foundTargetRevision) {
            if(!updatedNode) {
                throw new IllegalArgumentException("The node \"" + nodeAction + " " + nodePath + "\" was not found at revision " + targetRevision);
            }
            return;
        }

        if(revision.getNumber() == targetRevision) {
            foundTargetRevision = true;
        }
        super.consume(revision);
    }

    @Override
    public void consume(SvnNode node) {
        if(updatedNode) {
            super.consume(node);
            return;
        }

        if (nodeAction.equals(node.get(SvnNodeHeader.ACTION)) &&
                nodePath.equals(node.get(SvnNodeHeader.PATH))) {
            if(oldValue != null && !oldValue.equals(node.get(headerToChange))) {
                throw new IllegalArgumentException("The old value for the " + headerToChange.name() + " property is not \"" + oldValue + "\"");
            }
            node.getHeaders().put(headerToChange, newValue);
            updatedNode = true;
        }
        super.consume(node);
    }

    @Override
    public void finish() {
        if(!foundTargetRevision) {
            throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
        }

        foundTargetRevision = false;
        updatedNode = false;
        super.finish();
    }
}
