package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.TreeOfKnowledge;
import com.github.cstroe.svndumpgui.internal.AbstractRepositoryConsumer;
import com.github.cstroe.svndumpgui.internal.utility.range.MultiSpan;
import com.github.cstroe.svndumpgui.internal.utility.range.Span;
import com.github.cstroe.svndumpgui.internal.utility.range.SpanImpl;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNode;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNodeImpl;
import org.javatuples.Triplet;

import java.util.List;
import java.util.function.Predicate;

/**
 * It knows everything that has ever happened.  See {@link TreeOfKnowledge} for more documentation.
 *
 * @see com.github.cstroe.svndumpgui.api.TreeOfKnowledge
 */
public class TreeOfKnowledgeImpl extends AbstractRepositoryConsumer implements TreeOfKnowledge {

    private final CLTreeNodeImpl<Triplet<MultiSpan, String, Node>> root;

    private Predicate<Triplet<MultiSpan, String, Node>> inRevision(int revision) {
        return t ->  t.getValue0().contains(revision);
    }

    private Predicate<Triplet<MultiSpan, String, Node>> inRevisionWithLabel(int revision, String label) {
        return t ->  t.getValue0().contains(revision) && t.getValue1().equals(label);
    }

    public TreeOfKnowledgeImpl() {
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
                break;

            case "change":
            case "replace":
                final String nodeTextContentLength = node.get(NodeHeader.TEXT_CONTENT_LENGTH);
                if(nodeTextContentLength != null) {
                    deleteFromTree(node);
                    addToTree(node);
                }
                break;

            default:
                throw new IllegalArgumentException("Unhandled node action.");
        }
        super.consume(node);
    }

    private void addToTree(Node node) {
        final int currentRevision = node.getRevision().get().getNumber();

        String[] pathComponents = node.get(NodeHeader.PATH).split("/");

        CLTreeNode<Triplet<MultiSpan, String, Node>> currentRoot = root;
        for (String currentPath : pathComponents) {
            List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children =
                    currentRoot.getChildren(inRevisionWithLabel(currentRevision, currentPath));

            final Span spanToInfinity = new SpanImpl(currentRevision, Span.POSITIVE_INFINITY);
            if (children.size() == 0) {
                MultiSpan multiSpan = new MultiSpan();
                multiSpan.add(spanToInfinity);
                Triplet<MultiSpan, String, Node> r = Triplet.with(
                        multiSpan,
                        currentPath,
                        node);
                CLTreeNode<Triplet<MultiSpan, String, Node>> newNode = new CLTreeNodeImpl<>(r);
                currentRoot.addChild(newNode);
                currentRoot = newNode;
            } else if(children.size() == 1) {
                CLTreeNode<Triplet<MultiSpan, String, Node>> currentNode = children.get(0);
                currentNode.lookInside().getValue0().add(spanToInfinity);
                currentRoot = currentNode;
            } else {
                throw new IllegalArgumentException("Ambiguous node label.");
            }
        }

        // copied from
        String copiedFromRev = node.get(NodeHeader.COPY_FROM_REV);
        if (copiedFromRev != null) {
            int copyRevision = Integer.parseInt(copiedFromRev);
            String copyPath = node.get(NodeHeader.COPY_FROM_PATH);

            CLTreeNode<Triplet<MultiSpan, String, Node>> oldNode = findNode(copyRevision, copyPath);
            copyChildren(oldNode, currentRoot, copyRevision, currentRevision);
        }
    }

    private void copyChildren(CLTreeNode<Triplet<MultiSpan, String, Node>> fromNode,
                              CLTreeNode<Triplet<MultiSpan, String, Node>> toNode,
                              int sourceRevision, int targetRevision) {
        final Span spanToInfinity = new SpanImpl(targetRevision, Span.POSITIVE_INFINITY);

        List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children =
                fromNode.getChildren(inRevision(sourceRevision));
        for (CLTreeNode<Triplet<MultiSpan, String, Node>> child : children) {
            Triplet<MultiSpan, String, Node> d = child.lookInside();
            MultiSpan newMultiSpan = d.getValue0().clone();
            newMultiSpan.add(spanToInfinity);
            CLTreeNode<Triplet<MultiSpan, String, Node>> newChild = new CLTreeNodeImpl<>(Triplet.with(newMultiSpan, d.getValue1(), d.getValue2()));
            toNode.addChild(newChild);
            copyChildren(child, newChild, sourceRevision, targetRevision);
        }
    }

    private void deleteFromTree(Node node) {
        final int currentRevision = node.getRevision().get().getNumber();

        CLTreeNode<Triplet<MultiSpan, String, Node>> treeNode = findNode(currentRevision, node.get(NodeHeader.PATH));

        final int previousRevision = currentRevision - 1;
        cutoff(treeNode, currentRevision, previousRevision);
    }

    private void cutoff(CLTreeNode<Triplet<MultiSpan, String, Node>> currentRoot, int currentRevision, int previousRevision) {
        currentRoot.lookInside().getValue0().cutoff(previousRevision);
        List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children =
                currentRoot.getChildren(inRevision(currentRevision));
        for (CLTreeNode<Triplet<MultiSpan, String, Node>> child: children) {
            cutoff(child, currentRevision, previousRevision);
        }
    }

    private CLTreeNode<Triplet<MultiSpan, String, Node>> findNode(int revision, String path) {
        return findNode(revision, path.split("/"));
    }

    private CLTreeNode<Triplet<MultiSpan, String, Node>> findNode(int revision, String[] pathComponents) {
        CLTreeNode<Triplet<MultiSpan, String, Node>> currentRoot = root;
        for (String currentPath : pathComponents) {
            List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children =
                    currentRoot.getChildren(inRevisionWithLabel(revision, currentPath));
            if (children.size() == 0) {
                throw new IllegalArgumentException("cannot find: r" + revision + " '" + String.join("/", pathComponents) + "'");
            } else if(children.size() > 1)  {
                throw new IllegalArgumentException("ambiguous path: r" + revision + " '" + String.join("/", pathComponents) + "'");
            }

            currentRoot = children.get(0);
        }

        return currentRoot;
    }

    @Override
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
