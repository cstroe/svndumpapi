package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.*;
import com.github.cstroe.svndumpgui.internal.SvnDumpErrorImpl;

import java.util.*;

public class PathCollision implements SvnDumpValidator {
    private SvnDumpError error = null;

    @Override
    public boolean isValid(SvnDump dump) {
        Map<String, Integer> pathByRevision = new HashMap<>();
        for(SvnRevision revision : dump.getRevisions()) {
            for(SvnNode node : revision.getNodes()) {
                final String action = node.get(SvnNodeHeader.ACTION);
                final String kind = node.get(SvnNodeHeader.KIND);
                final String path = node.get(SvnNodeHeader.PATH);
                final String copyFromRevision = node.get(SvnNodeHeader.COPY_FROM_REV);
                final String copyFromPath = node.get(SvnNodeHeader.COPY_FROM_PATH);

                if("add".equals(action)) {
                    if(pathByRevision.containsKey(path)) {
                        String message = "Error at revision " + revision.getNumber() + "\n" +
                                "adding " + path + "\n" +
                                "but it was already added in revision " + pathByRevision.get(path);
                        error = new SvnDumpErrorImpl(message, revision, node);
                        return false;
                    }

                    pathByRevision.put(path, revision.getNumber());

                    if ("dir".equals(kind) && copyFromRevision != null) {
                        // add sub paths also
                        final String oldPrefix = copyFromPath + "/";
                        final String newPrefix = path + "/";
                        Set<String> subPaths = getSubPaths(pathByRevision.keySet(), copyFromPath);
                        for (String subPath : subPaths) {
                            final String newSubPath = newPrefix + subPath.substring(oldPrefix.length());
                            pathByRevision.put(newSubPath, revision.getNumber());
                        }
                    }
                } else if("delete".equals(action)) {
                    if(!pathByRevision.containsKey(path)) {
                        String message = "Error at revision " + revision.getNumber() + "\n" +
                                "deleting " + path + "\n" +
                                "but it does not exist";
                        error = new SvnDumpErrorImpl(message, revision, node);
                        return false;
                    }

                    pathByRevision.remove(path);

                    // remove any subpaths that may exist
                    Set<String> subPaths = getSubPaths(pathByRevision.keySet(), path);
                    for (String subPath : subPaths) {
                        pathByRevision.remove(subPath);
                    }
                }
            }
        }
        return true;
    }

    @Override
    public SvnDumpError getError() {
        return error;
    }

    public static Set<String> getSubPaths(final Set<String> currentPaths, final String path) {
        String prefix = path;
        if(!path.endsWith("/")) {
            prefix = path + "/";
        }

        Set<String> subPaths = new HashSet<>();
        for(String currentPath : currentPaths) {
            if(currentPath.startsWith(prefix)) {
                subPaths.add(currentPath);
            }
        }

        return subPaths;
    }
}
