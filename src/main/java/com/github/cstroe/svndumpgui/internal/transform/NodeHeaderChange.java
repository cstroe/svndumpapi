package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.*;

public class NodeHeaderChange implements SvnDumpMutator {

    private final int targetRevision;
    private final String nodeAction;
    private final String nodePath;
    private final SvnNodeHeader headerToChange;
    private final String oldValue;
    private final String newValue;

    public NodeHeaderChange(int targetRevision, String nodeAction, String path, SvnNodeHeader headerToChange, String oldValue, String newValue) {
        this.targetRevision = targetRevision;
        this.nodeAction = nodeAction;
        this.nodePath = path;
        this.headerToChange = headerToChange;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    @Override
    public void mutate(SvnDump dump) {
        for(SvnRevision revision : dump.getRevisions()) {
            if(revision.getNumber() == targetRevision) {
                for (SvnNode node : revision.getNodes()) {
                    if (nodeAction.equals(node.get(SvnNodeHeader.ACTION)) &&
                            nodePath.equals(node.get(SvnNodeHeader.PATH))) {
                        if(oldValue != null && !oldValue.equals(node.get(headerToChange))) {
                            throw new IllegalArgumentException("The old value for the " + headerToChange.name() + " property is not \"" + oldValue + "\"");
                        }
                        node.getHeaders().put(headerToChange, newValue);
                        return;
                    }
                }
                throw new IllegalArgumentException("The node \""+ nodeAction + " " + nodePath + "\" was not found at revision " + targetRevision);
            }
        }
        throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
    }
}
