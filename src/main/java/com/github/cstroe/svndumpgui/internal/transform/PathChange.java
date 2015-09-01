package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.*;

public class PathChange extends AbstractSvnDumpMutator {

    private final String oldPath;
    private final String newPath;

    public PathChange(String oldPath, String newPath) {
        this.oldPath = oldPath;
        this.newPath = newPath;
    }

    @Override
    public void mutate(SvnRevision revision) {
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
        }
    }

    @Override
    public void finish() {}
}
