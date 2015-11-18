package com.github.cstroe.svndumpgui.internal.utility.tree;

import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link CLTreeNode node in a Conditionally Linked Tree}.
 *
 * A conditionally linked tree is a Tree data structure with conditional
 * parent-to-child links.
 *
 * That is, whether a child belongs to the parent is conditioned on the
 * success of a given Predicate.  In this way node to node (parent to child)
 * traversal is controlled by the predicate, allowing different "views" of the
 * tree.
 */
public interface CLTreeNode<T> {
    void addChild(CLTreeNode<T> child);
    void removeChild(Predicate<T> condition);
    List<CLTreeNode<T>> getChildren(Predicate<T> condition);
    T lookInside();
}
