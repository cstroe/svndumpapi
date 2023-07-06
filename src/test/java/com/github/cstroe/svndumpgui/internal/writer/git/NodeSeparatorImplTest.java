package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import org.javatuples.Quartet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NodeSeparatorImplTest {
    @Test
    public void separateEmptyList() {
        NodeSeparator separator = new NodeSeparatorImpl();
        List<Quartet<ChangeType, String, String, Node>> list =
                separator.separate(new ArrayList<>());
        assertNotNull(list);
        assertTrue(list.isEmpty());
    }

    @Test
    public void separateMainBranchFromOtherBranch() {
        NodeImpl n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "some/file.txt");
        NodeImpl n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "branches/mybranch/some/file2.txt");

        NodeSeparatorImpl separator = new NodeSeparatorImpl();
        List<Quartet<ChangeType, String, String, Node>> list = separator.separate(listOf(n1, n2));

        assertEquals(2, list.size());
        assertEquals(Quartet.with(ChangeType.TRUNK, "main", "some/file.txt", n1), list.get(0));
        assertEquals(Quartet.with(ChangeType.BRANCH, "mybranch", "some/file2.txt", n2), list.get(1));
    }

    @Test
    public void detectBranchCreation() {
        NodeImpl n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "some/file.txt");
        NodeImpl n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "branches/mybranch");

        NodeSeparatorImpl separator = new NodeSeparatorImpl();
        List<Quartet<ChangeType, String, String, Node>> list = separator.separate(listOf(n1, n2));

        assertEquals(2, list.size());
        assertEquals(Quartet.with(ChangeType.TRUNK, "main", "some/file.txt", n1), list.get(0));
        assertEquals(Quartet.with(ChangeType.BRANCH_CREATE, "mybranch", null, n2), list.get(1));
    }

    @Test
    public void separateMainBranchFromTag() {
        NodeImpl n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "some/file.txt");
        NodeImpl n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "tags/mytag/some/file2.txt");

        NodeSeparatorImpl separator = new NodeSeparatorImpl();
        List<Quartet<ChangeType, String, String, Node>> list = separator.separate(listOf(n1, n2));

        assertEquals(2, list.size());
        assertEquals(Quartet.with(ChangeType.TRUNK, "main", "some/file.txt", n1), list.get(0));
        assertEquals(Quartet.with(ChangeType.TAG, "mytag", "some/file2.txt", n2), list.get(1));
    }

    @Test
    public void detectTagCreation() {
        NodeImpl n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "some/file.txt");
        NodeImpl n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "tags/mytag");

        NodeSeparatorImpl separator = new NodeSeparatorImpl();
        List<Quartet<ChangeType, String, String, Node>> list = separator.separate(listOf(n1, n2));

        assertEquals(2, list.size());
        assertEquals(Quartet.with(ChangeType.TRUNK, "main", "some/file.txt", n1), list.get(0));
        assertEquals(Quartet.with(ChangeType.TAG_CREATE, "mytag", null, n2), list.get(1));
    }

    private <T> List<T> listOf(T... elems) {
        return new ArrayList<>(Arrays.asList(elems));
    }
}