package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class NodeHeaderChange extends AbstractSvnDumpMutator {

    private int REVISION_NOT_FOUND = 0;
    private int FOUND_REVISION_BUT_DIDNT_UPDATE_NODE = 1;
    private int UPDATED_NODE = 2;

    private final int targetRevision;
    private final String nodeAction;
    private final String nodePath;
    private final SvnNodeHeader headerToChange;
    private final String oldValue;
    private final String newValue;

    private int state = REVISION_NOT_FOUND;

    public NodeHeaderChange(int targetRevision, String nodeAction, String path, SvnNodeHeader headerToChange, String oldValue, String newValue) {
        if(oldValue == null) {
            throw new IllegalArgumentException("Cannot accept null paramter: oldValue");
        }

        this.targetRevision = targetRevision;
        this.nodeAction = nodeAction;
        this.nodePath = path;
        this.headerToChange = headerToChange;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void consume(SvnRevision revision) {
        if(revision.getNumber() == targetRevision) {
            state = FOUND_REVISION_BUT_DIDNT_UPDATE_NODE;
        }
        super.consume(revision);
    }

    @Override
    public void endRevision(SvnRevision revision) {
        if(state == FOUND_REVISION_BUT_DIDNT_UPDATE_NODE) {
            throw new IllegalArgumentException("The node \"" + nodeAction + " " + nodePath + "\" was not found at revision " + targetRevision);
        }
        super.endRevision(revision);
    }

    @Override
    public void consume(SvnNode node) {
        if(state == FOUND_REVISION_BUT_DIDNT_UPDATE_NODE) {
            if (nodeAction.equals(node.get(SvnNodeHeader.ACTION)) && nodePath.equals(node.get(SvnNodeHeader.PATH))) {
                if(!oldValue.equals(node.get(headerToChange))) {
                    throw new IllegalArgumentException("The old value for the " + headerToChange.name() + " property is not \"" + oldValue + "\"");
                }
                node.getHeaders().put(headerToChange, newValue);
                state = UPDATED_NODE;
            }
        }

        super.consume(node);
    }

    @Override
    public void finish() {
        if(state == REVISION_NOT_FOUND) {
            throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
        }

        super.finish();
    }
}
