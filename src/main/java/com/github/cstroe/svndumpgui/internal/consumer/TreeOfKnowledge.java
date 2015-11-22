package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.internal.AbstractRepositoryConsumer;
import com.github.cstroe.svndumpgui.internal.utility.range.MultiSpan;
import com.github.cstroe.svndumpgui.internal.utility.range.Span;
import com.github.cstroe.svndumpgui.internal.utility.range.SpanImpl;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNode;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNodeImpl;
import org.javatuples.Triplet;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class TreeOfKnowledge extends AbstractRepositoryConsumer {

    private final CLTreeNodeImpl<Triplet<MultiSpan, String, Node>> root;

    private Predicate<Triplet<MultiSpan, String, Node>> inRevision(int revision) {
        return t ->  t.getValue0().contains(revision);
    }

    private Predicate<Triplet<MultiSpan, String, Node>> inRevisionWithLabel(int revision, String label) {
        return t ->  t.getValue0().contains(revision) && t.getValue1().equals(label);
    }

    public TreeOfKnowledge() {
        Span toInfinity = new SpanImpl(0, Span.POSITIVE_INFINITY);
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(toInfinity);
        this.root = new CLTreeNodeImpl<>(Triplet.with(multiSpan, "/", null));
    }

    CLTreeNode<Triplet<MultiSpan, String, Node>> getRoot() {
        return root;
    }

    @Override
    public void consume(Node node) {
        final String nodeAction = node.get(NodeHeader.ACTION);
        switch(nodeAction) {
            case "add":
                addToTree(node);
                break;

            case "delete":
                deleteFromTree(node);

            default:
                throw new IllegalArgumentException("Unhandled node action.");
        }
    }

    private void addToTree(Node node) {
        final int revision = node.getRevision().get().getNumber();

        String[] pathComponents = node.get(NodeHeader.PATH).split("/");

        CLTreeNode<Triplet<MultiSpan, String, Node>> currentRoot = root;
        for (String currentPath : pathComponents) {
            List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children = currentRoot.getChildren(inRevision(revision));
            Optional<CLTreeNode<Triplet<MultiSpan, String, Node>>> currentNode =
                    children.parallelStream().filter(n -> n.lookInside().getValue1().equals(currentPath))
                            .findAny();

            final Span toInfinity = new SpanImpl(revision, Span.POSITIVE_INFINITY);
            if (!currentNode.isPresent()) {
                MultiSpan multiSpan = new MultiSpan();
                multiSpan.add(toInfinity);
                Triplet<MultiSpan, String, Node> r = Triplet.with(
                        multiSpan,
                        currentPath,
                        node);
                CLTreeNode<Triplet<MultiSpan, String, Node>> newNode = new CLTreeNodeImpl<>(r);
                currentRoot.addChild(newNode);
                currentRoot = newNode;
            } else {
                currentNode.get().lookInside().getValue0().add(toInfinity);
            }
        }
    }

    private void deleteFromTree(Node node) {

    }

    /**
     * @return The node that introduced the path in the given revision.
     *         null if the path didn't exist in the given revision.
     */
    public Node tellMeAbout(int revision, String path) {
        String[] pathComponents = path.split("/");

        CLTreeNode<Triplet<MultiSpan, String, Node>> currentRoot = root;
        for(String currentPath : pathComponents) {
            List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children = currentRoot.getChildren(inRevisionWithLabel(revision, currentPath));

            if(children.size() == 0) {
                return null;
            }

            if(children.size() != 1) {
                throw new RuntimeException("Ambiguous label.  You have hit a condition that I thought should never happen.  Either this is a bug, or this program is not advanced enough to handle your data.");
            }

            currentRoot = children.get(0);
        }

        return currentRoot.lookInside().getValue2();
    }
}
