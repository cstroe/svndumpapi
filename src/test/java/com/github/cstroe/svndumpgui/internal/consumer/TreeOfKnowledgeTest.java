package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.utility.range.MultiSpan;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNode;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNodeImpl;
import org.javatuples.Triplet;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class TreeOfKnowledgeTest {
    @Test
    public void empty() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/empty.dump"), tok);

        CLTreeNode<Triplet<MultiSpan, String, Node>> root = tok.getRoot();
        assertNotNull(root);

        List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children = root.getChildren(n -> true);
        assertThat(children.size(), is(0));
    }

    @Test
    public void add_file() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file.dump"), tok);

        CLTreeNode<Triplet<MultiSpan, String, Node>> root = tok.getRoot();
        assertNotNull(root);

        List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children = root.getChildren(n -> true);
        assertThat(children.size(), is(1));

        Node n = tok.tellMeAbout(1, "README.txt");
        assertNotNull(n);
        assertThat(n.get(NodeHeader.MD5), is("4221d002ceb5d3c9e9137e495ceaa647"));
        assertThat(n.get(NodeHeader.SHA1), is("804d716fc5844f1cc5516c8f0be7a480517fdea2"));
    }
}
