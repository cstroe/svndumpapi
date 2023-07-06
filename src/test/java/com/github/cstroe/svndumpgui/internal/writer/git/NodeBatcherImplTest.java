package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.javatuples.Triplet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class NodeBatcherImplTest {
    @Test
    public void batchEmptyList() {
        NodeBatcher batcher = new NodeBatcherImpl();
        List<Pair<ChangeType, List<Triplet<String, String, Node>>>> batches =
                batcher.batch(new ArrayList<>());
        assertTrue(batches.isEmpty());
    }

    @Test
    public void batchAllTypes() {
        NodeBatcher batcher = new NodeBatcherImpl();
        List<Quartet<ChangeType, String, String, Node>> changes = new ArrayList<>();
        Node n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "node1");
        Quartet<ChangeType, String, String, Node> q1 = Quartet.with(ChangeType.TRUNK, "q1", "p1", n1);
        Node n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "node2");
        Quartet<ChangeType, String, String, Node> q2 = Quartet.with(ChangeType.BRANCH_CREATE, "q2", "p2", n2);
        Node n3 = new NodeImpl();
        n3.getHeaders().put(NodeHeader.PATH, "node3");
        Quartet<ChangeType, String, String, Node> q3 = Quartet.with(ChangeType.BRANCH, "q3", "p3", n3);
        Node n4 = new NodeImpl();
        n4.getHeaders().put(NodeHeader.PATH, "node4");
        Quartet<ChangeType, String, String, Node> q4 = Quartet.with(ChangeType.TAG_CREATE, "q4", "p4", n4);
        Node n5 = new NodeImpl();
        n5.getHeaders().put(NodeHeader.PATH, "node5");
        Quartet<ChangeType, String, String, Node> q5 = Quartet.with(ChangeType.TAG, "q5", "p5", n5);

        changes.add(q1);
        changes.add(q2);
        changes.add(q3);
        changes.add(q4);
        changes.add(q5);
        List<Pair<ChangeType, List<Triplet<String, String, Node>>>> batches =
                batcher.batch(changes);

        assertNotNull(changes);
        assertEquals(5, batches.size());
        Pair<ChangeType, List<Triplet<String, String, Node>>> b1 = batches.get(0);
        assertEquals(ChangeType.TRUNK, b1.getValue0());
        assertEquals(1, b1.getValue1().size());
        assertEquals("q1", b1.getValue1().get(0).getValue0());
        assertEquals("p1", b1.getValue1().get(0).getValue1());
        assertEquals("node1", b1.getValue1().get(0).getValue2().getPath().get());
        Pair<ChangeType, List<Triplet<String, String, Node>>> b2 = batches.get(1);
        assertEquals(ChangeType.BRANCH_CREATE, b2.getValue0());
        assertEquals(1, b2.getValue1().size());
        assertEquals("q2", b2.getValue1().get(0).getValue0());
        assertEquals("p2", b2.getValue1().get(0).getValue1());
        assertEquals("node2", b2.getValue1().get(0).getValue2().getPath().get());
        Pair<ChangeType, List<Triplet<String, String, Node>>> b3 = batches.get(2);
        assertEquals(ChangeType.BRANCH, b3.getValue0());
        assertEquals(1, b3.getValue1().size());
        assertEquals("q3", b3.getValue1().get(0).getValue0());
        assertEquals("p3", b3.getValue1().get(0).getValue1());
        assertEquals("node3", b3.getValue1().get(0).getValue2().getPath().get());
        Pair<ChangeType, List<Triplet<String, String, Node>>> b4 = batches.get(3);
        assertEquals(ChangeType.TAG_CREATE, b4.getValue0());
        assertEquals(1, b4.getValue1().size());
        assertEquals("q4", b4.getValue1().get(0).getValue0());
        assertEquals("p4", b4.getValue1().get(0).getValue1());
        assertEquals("node4", b4.getValue1().get(0).getValue2().getPath().get());
        Pair<ChangeType, List<Triplet<String, String, Node>>> b5 = batches.get(4);
        assertEquals(ChangeType.TAG, b5.getValue0());
        assertEquals(1, b5.getValue1().size());
        assertEquals("q5", b5.getValue1().get(0).getValue0());
        assertEquals("p5", b5.getValue1().get(0).getValue1());
        assertEquals("node5", b5.getValue1().get(0).getValue2().getPath().get());
    }

    @Test
    public void batchMultipleChanges() {
        NodeBatcher batcher = new NodeBatcherImpl();
        List<Quartet<ChangeType, String, String, Node>> changes = new ArrayList<>();
        Node n1 = new NodeImpl();
        n1.getHeaders().put(NodeHeader.PATH, "node1");
        Quartet<ChangeType, String, String, Node> q1 = Quartet.with(ChangeType.TRUNK, "q1", "p1", n1);
        Node n2 = new NodeImpl();
        n2.getHeaders().put(NodeHeader.PATH, "node2");
        Quartet<ChangeType, String, String, Node> q2 = Quartet.with(ChangeType.TRUNK, "q2", "p2", n2);
        Node n3 = new NodeImpl();
        n3.getHeaders().put(NodeHeader.PATH, "node3");
        Quartet<ChangeType, String, String, Node> q3 = Quartet.with(ChangeType.BRANCH, "q3", "p3", n3);
        Node n4 = new NodeImpl();
        n4.getHeaders().put(NodeHeader.PATH, "node4");
        Quartet<ChangeType, String, String, Node> q4 = Quartet.with(ChangeType.TRUNK, "q4", "p4", n4);
        Node n5 = new NodeImpl();
        n5.getHeaders().put(NodeHeader.PATH, "node5");
        Quartet<ChangeType, String, String, Node> q5 = Quartet.with(ChangeType.TRUNK, "q5", "p5", n5);

        changes.add(q1);
        changes.add(q2);
        changes.add(q3);
        changes.add(q4);
        changes.add(q5);
        List<Pair<ChangeType, List<Triplet<String, String, Node>>>> batches =
                batcher.batch(changes);

        assertNotNull(changes);
        assertEquals(3, batches.size());
        Pair<ChangeType, List<Triplet<String, String, Node>>> b1 = batches.get(0);
        assertEquals(ChangeType.TRUNK, b1.getValue0());
        assertEquals(2, b1.getValue1().size());
        assertEquals("q1", b1.getValue1().get(0).getValue0());
        assertEquals("p1", b1.getValue1().get(0).getValue1());
        assertEquals("node1", b1.getValue1().get(0).getValue2().getPath().get());
        assertEquals("q2", b1.getValue1().get(1).getValue0());
        assertEquals("p2", b1.getValue1().get(1).getValue1());
        assertEquals("node2", b1.getValue1().get(1).getValue2().getPath().get());

        Pair<ChangeType, List<Triplet<String, String, Node>>> b2 = batches.get(1);
        assertEquals(ChangeType.BRANCH, b2.getValue0());
        assertEquals(1, b2.getValue1().size());
        assertEquals("q3", b2.getValue1().get(0).getValue0());
        assertEquals("p3", b2.getValue1().get(0).getValue1());
        assertEquals("node3", b2.getValue1().get(0).getValue2().getPath().get());

        Pair<ChangeType, List<Triplet<String, String, Node>>> b3 = batches.get(2);
        assertEquals(ChangeType.TRUNK, b3.getValue0());
        assertEquals(2, b3.getValue1().size());
        assertEquals("q4", b3.getValue1().get(0).getValue0());
        assertEquals("p4", b3.getValue1().get(0).getValue1());
        assertEquals("node4", b3.getValue1().get(0).getValue2().getPath().get());
        assertEquals("q5", b3.getValue1().get(1).getValue0());
        assertEquals("p5", b3.getValue1().get(1).getValue1());
        assertEquals("node5", b3.getValue1().get(1).getValue2().getPath().get());
    }
}