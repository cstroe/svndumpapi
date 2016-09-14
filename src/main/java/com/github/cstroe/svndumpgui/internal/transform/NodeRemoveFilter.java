package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.RepositoryFilter;
import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;

public class NodeRemoveFilter implements RepositoryFilter {

    private final int targetRevision;
    private final String action;
    private final String path;

    private boolean foundTargetRevision = false;
    private boolean removedNode = false;
    private boolean inRemovedNode = false;

    public NodeRemoveFilter(int targetRevision, String action, String nodePath) {
        this.targetRevision = targetRevision;
        if("add".equals(action) || "delete".equals(action) || "change".equals(action) || "replace".equals(action)) {
            this.action = action;
        } else {
            throw new RuntimeException("Cannot match on invalid action. Found '" + String.valueOf(action) + "' but expecting (\"change\" | \"add\" | \"delete\" | \"replace\")");
        }
        this.path = nodePath;
    }

    @Override
    public void consume(Revision revision) {
        if(foundTargetRevision) {
            if(!removedNode) {
                throw new IllegalArgumentException("The node \"" + action + " " + path +
                        "\" was not found at revision " + targetRevision);
            }
        }

        if(revision.getNumber() == targetRevision) {
            foundTargetRevision = true;
        }
    }

    @Override
    public void consume(Node node) {
        boolean nodeMatches = foundTargetRevision && !removedNode &&
                action.equals(node.get(NodeHeader.ACTION)) &&
                path.equals(node.get(NodeHeader.PATH));

        if(nodeMatches) {
            removedNode = true;
            inRemovedNode = true;
            return;
        }
    }

    @Override
    public void consume(ContentChunk chunk) {
//		OH GOD THERE IS LOGIC HERE
//        if(!inRemovedNode) {
//            super.consume(chunk);
//        }
    }

	@Override
	public void endRevision(Revision revision) {
	}

    @Override
    public void endChunks() {
//		OH GOD THERE IS LOGIC HERE
//        if(!inRemovedNode) {
//            super.endChunks();
//        }
    }

    @Override
    public void endNode(Node node) {
//		OH GOD THERE IS LOGIC HERE
//        if(inRemovedNode) {
//            inRemovedNode = false;
//        } else {
//            super.endNode(node);
//        }
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
