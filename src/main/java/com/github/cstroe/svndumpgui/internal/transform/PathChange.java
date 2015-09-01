package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;

public class PathChange implements SvnDumpMutator {

    private final String oldPath;
    private final String newPath;

    public PathChange(String oldPath, String newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    @Override
    public void mutateRevision(SvnRevision revision) {
        if(revision.getProperties().containsKey(SvnProperty.MERGEINFO)) {
            final String mergeInfo = revision.get(SvnProperty.MERGEINFO);
            if(mergeInfo.startsWith(oldPath)) {
                final String newMergeInfo = newPath + mergeInfo.substring(oldPath.length());
                revision.getProperties().put(SvnProperty.MERGEINFO, newMergeInfo);
            }
        }

        for(SvnNode node : revision.getNodes()) {
            final String nodePath = node.get(SvnNodeHeader.PATH);
            if(nodePath.startsWith(oldPath)) {
                final String changed = newPath + nodePath.substring(oldPath.length());
                node.getHeaders().put(SvnNodeHeader.PATH, changed);
            }

            if(node.getHeaders().containsKey(SvnNodeHeader.COPY_FROM_PATH)) {
                final String copyPath = node.get(SvnNodeHeader.COPY_FROM_PATH);
                if(copyPath.startsWith(oldPath)) {
                    final String changed = newPath + copyPath.substring(oldPath.length());
                    node.getHeaders().put(SvnNodeHeader.COPY_FROM_PATH, changed);
                }
            }

            if(node.getProperties() != null && node.getProperties().containsKey(SvnProperty.MERGEINFO)) {
                final String mergeInfo = node.getProperties().get(SvnProperty.MERGEINFO);
                if(mergeInfo.startsWith(oldPath)) {
                    final String newMergeInfo = newPath + mergeInfo.substring(oldPath.length());
                    node.getProperties().put(SvnProperty.MERGEINFO, newMergeInfo);
                }

                final String leadingSlashPath = "/" + oldPath;
                if(mergeInfo.startsWith(leadingSlashPath)) {
                    final String newMergeInfo = "/" + newPath + mergeInfo.substring(oldPath.length() + 1);
                    node.getProperties().put(SvnProperty.MERGEINFO, newMergeInfo);
                }
            }
        }
    }

    @Override
    public void finish() {}
}
