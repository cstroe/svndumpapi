package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.TreeOfKnowledge;

/**
 * Creates a mechanism to allow sharing of a
 * single {@link TreeOfKnowledge} object.
 */
public interface RequiresTreeOfKnowledge {
    void setTreeOfKnowledge(TreeOfKnowledge tree);
}
