package com.github.cstroe.svndumpgui.internal.utility.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CLTreeNodeImpl<T> implements CLTreeNode<T> {
    private List<CLTreeNode<T>> children = new ArrayList<>();
    final T objectInside;

    public CLTreeNodeImpl(T objectInside) {
        this.objectInside = objectInside;
    }

    @Override
    public void addChild(CLTreeNode<T> child) {
        children.add(child);
    }

    @Override
    public void removeChild(Predicate<T> condition) {
        children = children.parallelStream()
                .filter(child -> !condition.test(child.lookInside()))
                .collect(Collectors.toList());
    }

    @Override
    public List<CLTreeNode<T>> getChildren(Predicate<T> condition) {
        return children.stream()
                .filter(c -> condition.test(c.lookInside()))
                .collect(Collectors.toList());
    }

    @Override
    public T lookInside() {
        return objectInside;
    }

    @Override
    public String toString() {
        return "CLTreeNodeImpl{" + objectInside.toString() + "}";
    }
}
