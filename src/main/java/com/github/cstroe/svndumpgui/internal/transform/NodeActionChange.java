package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.*;

public class NodeActionChange implements SvnDumpMutator {

    private final int targetRevision;
    private final String action;
    private final String path;
    private final String newAction;


    public NodeActionChange(int targetRevision, String action, String path, String newAction) {
        this.targetRevision = targetRevision;
        this.action = action;
        this.path = path;
        this.newAction = newAction;
    }

    @Override
    public void mutate(SvnDump dump) {
        for(SvnRevision revision : dump.getRevisions()) {
            if(revision.getNumber() == targetRevision) {
                for (SvnNode node : revision.getNodes()) {
                    if (action.equals(node.get(SvnNodeHeader.ACTION)) &&
                            path.equals(node.get(SvnNodeHeader.PATH))) {
                        node.getHeaders().put(SvnNodeHeader.ACTION, newAction);
                        return;
                    }
                }
                throw new IllegalArgumentException("The node \""+ action + " " + path + "\" was not found at revision " + targetRevision);
            }
        }
        throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
    }
}
