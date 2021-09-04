package com.github.cstroe.svndumpgui.internal.utility.tree;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;

public class CLTreeNodeImplTest {
    @Test
    public void simple_create_node() {
        CLTreeNode<String> node = new CLTreeNodeImpl<>("test");
        assertThat(node.lookInside(), is(equalTo("test")));
    }

    @Test
    public void get_children_respects_predicate() {
        CLTreeNode<String> parentNode = new CLTreeNodeImpl<>("parent");
        parentNode.addChild(new CLTreeNodeImpl<>("child1"));
        parentNode.addChild(new CLTreeNodeImpl<>("child2"));
        parentNode.addChild(new CLTreeNodeImpl<>("child3"));
        parentNode.addChild(new CLTreeNodeImpl<>("not_a_child"));
        parentNode.addChild(new CLTreeNodeImpl<>("again, not a child"));

        List<CLTreeNode<String>> children = parentNode.getChildren(s -> s.startsWith("child"));
        assertThat(children.size(), is(3));
        List<String> objectsInside = children.stream().map(CLTreeNode::lookInside).collect(Collectors.toList());
        assertTrue(objectsInside.contains("child1"));
        assertTrue(objectsInside.contains("child2"));
        assertTrue(objectsInside.contains("child3"));
    }

    @Test
    public void no_children() {
        CLTreeNode<String> parentNode = new CLTreeNodeImpl<>("parent");
        assertThat(parentNode.getChildren(t -> true).size(), is(0));
    }

    @Test(expected = NullPointerException.class)
    public void does_not_allow_null_predicate() {
        CLTreeNode<String> parentNode = new CLTreeNodeImpl<>("node");
        parentNode.addChild(new CLTreeNodeImpl<>("child"));
        parentNode.getChildren(null);
    }

    @Test
    public void remove_child() {
        CLTreeNode<String> parentNode = new CLTreeNodeImpl<>("parent");
        parentNode.addChild(new CLTreeNodeImpl<>("child1"));
        parentNode.addChild(new CLTreeNodeImpl<>("child2"));
        parentNode.addChild(new CLTreeNodeImpl<>("child3"));
        parentNode.addChild(new CLTreeNodeImpl<>("not_a_child"));
        parentNode.addChild(new CLTreeNodeImpl<>("again, not a child"));

        assertThat(parentNode.getChildren(c->true).size(), is(5));
        parentNode.removeChild("child1"::equals);
        parentNode.removeChild("not_a_child"::equals);
        assertThat(parentNode.getChildren(c->true).size(), is(3));
        List<String> objectsInside = parentNode.getChildren(c->true)
                .stream().map(CLTreeNode::lookInside).collect(Collectors.toList());
        assertTrue(objectsInside.contains("child2"));
        assertTrue(objectsInside.contains("child3"));
        assertTrue(objectsInside.contains("again, not a child"));
    }

    @Test
    public void to_string() {
        final String aString = "string";
        CLTreeNodeImpl<String> clTreeNode = new CLTreeNodeImpl<>(aString);
        assertThat(clTreeNode.toString(), is(equalTo("CLTreeNodeImpl{string}")));
    }
}