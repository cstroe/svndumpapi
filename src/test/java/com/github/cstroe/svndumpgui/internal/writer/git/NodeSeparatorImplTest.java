package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import org.javatuples.Triplet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class NodeSeparatorImplTest {
    @Test
    public void separateEmptyList() {
        NodeSeparator separator = new NodeSeparatorImpl();
        List<Triplet<ChangeType, String, String>> list =
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
        List<Triplet<ChangeType, String, String>> list = separator.separate(listOf(n1, n2));

        assertEquals(2, list.size());
        assertEquals(Triplet.with(ChangeType.MAIN, "main", "some/file.txt"), list.get(0));
        assertEquals(Triplet.with(ChangeType.BRANCH, "mybranch", "some/file2.txt"), list.get(1));
    }

    @Test
    public void separateMainBranchFromTag() {
        NodeImpl n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "some/file.txt");
        NodeImpl n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "tags/mytag/some/file2.txt");

        NodeSeparatorImpl separator = new NodeSeparatorImpl();
        List<Triplet<ChangeType, String, String>> list = separator.separate(listOf(n1, n2));

        assertEquals(2, list.size());
        assertEquals(Triplet.with(ChangeType.MAIN, "main", "some/file.txt"), list.get(0));
        assertEquals(Triplet.with(ChangeType.TAG, "mytag", "some/file2.txt"), list.get(1));
    }

    private <T> List<T> listOf(T... elems) {
        return new ArrayList<>(Arrays.asList(elems));
    }
}