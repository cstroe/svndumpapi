package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.utility.range.MultiSpan;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNode;
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

    @Test
    public void nested_directories() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        {
            Node n = tok.tellMeAbout(1, "test");
            assertThat(n.get(NodeHeader.ACTION), is("add"));
            assertThat(n.get(NodeHeader.PATH), is("test"));
        }{
            Node n = tok.tellMeAbout(1, "test/file1.txt");
            assertThat(n.get(NodeHeader.ACTION), is("add"));
            assertThat(n.get(NodeHeader.PATH), is("test/file1.txt"));
            assertThat(n.get(NodeHeader.MD5), is("d41d8cd98f00b204e9800998ecf8427e"));
            assertThat(n.get(NodeHeader.SHA1), is("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        }{
            Node n = tok.tellMeAbout(1, "test/file2.txt");
            assertThat(n.get(NodeHeader.ACTION), is("add"));
            assertThat(n.get(NodeHeader.PATH), is("test/file2.txt"));
            assertThat(n.get(NodeHeader.MD5), is("d41d8cd98f00b204e9800998ecf8427e"));
            assertThat(n.get(NodeHeader.SHA1), is("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        }{
            Node n = tok.tellMeAbout(1, "test/innerdir/file3.txt");
            assertThat(n.get(NodeHeader.ACTION), is("add"));
            assertThat(n.get(NodeHeader.PATH), is("test/innerdir/file3.txt"));
            assertThat(n.get(NodeHeader.MD5), is("d41d8cd98f00b204e9800998ecf8427e"));
            assertThat(n.get(NodeHeader.SHA1), is("da39a3ee5e6b4b0d3255bfef95601890afd80709"));
        }
    }
}