package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.RepositoryValidationError;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.utility.Pair;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PathCollisionValidator extends AbstractRepositoryValidator {
    private RepositoryValidationError error = null;

    private Map<Integer, Map<String, Pair<Integer, Node>>> revisionSnapshots = new HashMap<>();
    private Revision previousRevision = null;

    @Override
    public void consume(Revision revision) {
        if(error != null) {
            super.consume(revision);
            return;
        }

        // path -> (revision added, node that added it)
        Map<String, Pair<Integer, Node>> currentRevisionPaths = new HashMap<>();

        if(previousRevision != null) {
            currentRevisionPaths.putAll(revisionSnapshots.get(previousRevision.getNumber()));
        }
        
        revisionSnapshots.put(revision.getNumber(), currentRevisionPaths);
        previousRevision = revision;

        super.consume(revision);
    }

    @Override
    public void consume(Node node) {
        if(error != null) {
            super.consume(node);
            return;
        }
        
        final Revision revision = node.getRevision().get();
        
        // path -> (revision added, node that added it)
        Map<String, Pair<Integer, Node>> currentRevisionPaths =
                revisionSnapshots.get(revision.getNumber());
        
        final String action = node.get(NodeHeader.ACTION);
        final String kind = node.get(NodeHeader.KIND);
        final String path = node.get(NodeHeader.PATH);
        final String copyFromRevision = node.get(NodeHeader.COPY_FROM_REV);
        final String copyFromPath = node.get(NodeHeader.COPY_FROM_PATH);

        if("add".equals(action)) {
            if(currentRevisionPaths.containsKey(path)) {
                String message = "Error at revision " + revision.getNumber() + "\n" +
                        "adding " + path + "\n" +
                        "but it was already added in revision " + currentRevisionPaths.get(path).first +
                        " by this node:\n" +
                        currentRevisionPaths.get(path).second;
                error = new RepositoryValidationErrorImpl(message, revision.getNumber(), node);
            }

            if(copyFromRevision != null) {
                Map sourceSnapshot = revisionSnapshots.get(Integer.parseInt(copyFromRevision));
                if(!sourceSnapshot.containsKey(copyFromPath)) {
                    String message = "Error at revision " + revision.getNumber() + "\n" +
                            "adding " + path + "\n" +
                            "from " + copyFromPath + "@" + copyFromRevision + "\n" +
                            "but it doesn't exist in revision " + copyFromRevision;
                    error = new RepositoryValidationErrorImpl(message, revision.getNumber(), node);
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
                error = new RepositoryValidationErrorImpl(message, revision.getNumber(), node);
            }

            currentRevisionPaths.remove(path);

            // remove any subpaths that may exist
            Set<String> subPaths = getSubPaths(currentRevisionPaths.keySet(), path);
            for (String subPath : subPaths) {
                currentRevisionPaths.remove(subPath);
            }
        }

        super.consume(node);
    }

    @Override
    public boolean isValid() {
        return error == null;
    }

    @Override
    public RepositoryValidationError getError() {
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
