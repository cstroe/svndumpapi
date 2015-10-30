package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;

public class PathChange extends AbstractRepositoryMutator {

    private final String oldPath;
    private final String newPath;

    public PathChange(String oldPath, String newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    @Override
    public void consume(Revision revision) {
        if(revision.getProperties().containsKey(Property.MERGEINFO)) {
            final String mergeInfo = revision.get(Property.MERGEINFO);
            if(mergeInfo.startsWith(oldPath)) {
                final String newMergeInfo = newPath + mergeInfo.substring(oldPath.length());
                revision.getProperties().put(Property.MERGEINFO, newMergeInfo);
            }
        }
        super.consume(revision);
    }

    @Override
    public void consume(Node node) {
        final String nodePath = node.get(NodeHeader.PATH);
        if(nodePath.startsWith(oldPath)) {
            final String changed = newPath + nodePath.substring(oldPath.length());
            node.getHeaders().put(NodeHeader.PATH, changed);
        }

        if(node.getHeaders().containsKey(NodeHeader.COPY_FROM_PATH)) {
            final String copyPath = node.get(NodeHeader.COPY_FROM_PATH);
            if(copyPath.startsWith(oldPath)) {
                final String changed = newPath + copyPath.substring(oldPath.length());
                node.getHeaders().put(NodeHeader.COPY_FROM_PATH, changed);
            }
        }

        if(node.getProperties() != null && node.getProperties().containsKey(Property.MERGEINFO)) {
            final String mergeInfo = node.getProperties().get(Property.MERGEINFO);
            if(mergeInfo.startsWith(oldPath)) {
                final String newMergeInfo = newPath + mergeInfo.substring(oldPath.length());
                node.getProperties().put(Property.MERGEINFO, newMergeInfo);
            }

            final String leadingSlashPath = "/" + oldPath;
            if(mergeInfo.startsWith(leadingSlashPath)) {
                final String newMergeInfo = "/" + newPath + mergeInfo.substring(oldPath.length() + 1);
                node.getProperties().put(Property.MERGEINFO, newMergeInfo);
            }
        }
        super.consume(node);
    }
}
