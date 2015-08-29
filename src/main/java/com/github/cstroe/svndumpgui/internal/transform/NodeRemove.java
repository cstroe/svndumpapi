package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.*;

import java.util.Iterator;

public class NodeRemove implements SvnDumpMutator {

    private final int targetRevision;
    private final String action;
    private final String path;

    public NodeRemove(int targetRevision, String action, String nodePath) {
        this.targetRevision = targetRevision;
        this.action = action;
        this.path = nodePath;
    }

    @Override
    public void mutate(SvnDump dump) {
        for(SvnRevision revision : dump.getRevisions()) {
            if(revision.getNumber() == targetRevision) {
                Iterator<SvnNode> iterator = revision.getNodes().iterator();
                while(iterator.hasNext()) {
                    SvnNode node = iterator.next();
                    if(action.equals(node.get(SvnNodeHeader.ACTION)) &&
                       path.equals(node.get(SvnNodeHeader.PATH))) {
                        iterator.remove();
                        return;
                    }
                }
                throw new IllegalArgumentException("The node was not found at revision " + targetRevision);
            }
        }
        throw new IllegalArgumentException("Revision " + targetRevision + " was not found.");
    }
}
