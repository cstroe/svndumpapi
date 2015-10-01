package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDumpValidationError;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.internal.SvnDumpValidationErrorImpl;
import com.github.cstroe.svndumpgui.internal.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PathCollision extends AbstractSvnDumpValidator {
    private SvnDumpValidationError error = null;

    private Map<Integer, Map<String, Pair<Integer, SvnNode>>> revisionSnapshots = new HashMap<>();
    private SvnRevision previousRevision = null;

    @Override
    public void consume(SvnRevision revision) {
        if(error != null) {
            return;
        }

        // path -> (revision added, node that added it)
        Map<String, Pair<Integer, SvnNode>> currentRevisionPaths = new HashMap<>();

        if(previousRevision != null) {
            currentRevisionPaths.putAll(revisionSnapshots.get(previousRevision.getNumber()));
        }
        
        revisionSnapshots.put(revision.getNumber(), currentRevisionPaths);
        previousRevision = revision;
    }

    @Override
    public void consume(SvnNode node) {
        if(error != null) {
            return;
        }
        
        final SvnRevision revision = node.getRevision().get();
        
        // path -> (revision added, node that added it)
        Map<String, Pair<Integer, SvnNode>> currentRevisionPaths = 
                revisionSnapshots.get(revision.getNumber());
        
        final String action = node.get(SvnNodeHeader.ACTION);
        final String kind = node.get(SvnNodeHeader.KIND);
        final String path = node.get(SvnNodeHeader.PATH);
        final String copyFromRevision = node.get(SvnNodeHeader.COPY_FROM_REV);
        final String copyFromPath = node.get(SvnNodeHeader.COPY_FROM_PATH);

        if("add".equals(action)) {
            if(currentRevisionPaths.containsKey(path)) {
                String message = "Error at revision " + revision.getNumber() + "\n" +
                        "adding " + path + "\n" +
                        "but it was already added in revision " + currentRevisionPaths.get(path).first +
                        " by this node:\n" +
                        currentRevisionPaths.get(path).second;
                error = new SvnDumpValidationErrorImpl(message, revision.getNumber(), node);
            }

            if(copyFromRevision != null) {
                Map sourceSnapshot = revisionSnapshots.get(Integer.parseInt(copyFromRevision));
                if(!sourceSnapshot.containsKey(copyFromPath)) {
                    String message = "Error at revision " + revision.getNumber() + "\n" +
                            "adding " + path + "\n" +
                            "from " + copyFromPath + "@" + copyFromRevision + "\n" +
                            "but it doesn't exist in revision " + copyFromRevision;
                    error = new SvnDumpValidationErrorImpl(message, revision.getNumber(), node);
                }
            }

            currentRevisionPaths.put(path, Pair.of(revision.getNumber(), node));

            if ("dir".equals(kind) && copyFromRevision != null) {
                // add sub paths also
                final String oldPrefix = copyFromPath + "/";
                final String newPrefix = path + "/";
                Set<String> subPaths = getSubPaths(revisionSnapshots.get(Integer.parseInt(copyFromRevision)).keySet(), copyFromPath);
                for (String subPath : subPaths) {
                    final String newSubPath = newPrefix + subPath.substring(oldPrefix.length());
                    currentRevisionPaths.put(newSubPath, Pair.of(revision.getNumber(), node));
                }
            }
        } else if("delete".equals(action)) {
            if(!currentRevisionPaths.containsKey(path)) {
                String message = "Error at revision " + revision.getNumber() + "\n" +
                        "deleting " + path + "\n" +
                        "but it does not exist";
                error = new SvnDumpValidationErrorImpl(message, revision.getNumber(), node);
            }

            currentRevisionPaths.remove(path);

            // remove any subpaths that may exist
            Set<String> subPaths = getSubPaths(currentRevisionPaths.keySet(), path);
            for (String subPath : subPaths) {
                currentRevisionPaths.remove(subPath);
            }
        }
    }

    @Override
    public boolean isValid() {
        return error == null;
    }

    @Override
    public SvnDumpValidationError getError() {
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
