package com.github.cstroe.svndumpgui.api;

/**
 * Keeps track of Nodes that existed in previous revisions.
 *
 * Some {@link Node nodes} reference nodes that
 * existed in previous revisions.  For example, when moving
 * a file, the node will have a {@link NodeHeader#COPY_FROM_REV COPY_FROM_REV}
 * and a {@link NodeHeader#COPY_FROM_PATH COPY_FROM_PATH}
 * that references the old name.
 *
 * This class keeps track of those previous nodes so that they
 * may be accessed.
 */
public interface TreeOfKnowledge extends RepositoryConsumer {
    /**
     * @param revision a past revision number
     * @param path the full path of a file or directory in the SVN repository
     *
     * @return The node that introduced the path in the given revision.
     *         null if the path didn't exist in the given revision.
     */
    Node tellMeAbout(int revision, String path);
}
