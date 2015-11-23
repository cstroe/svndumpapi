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
import static org.hamcrest.core.IsEqual.equalTo;
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
        assertThat(n.get(NodeHeader.MD5), is(equalTo("4221d002ceb5d3c9e9137e495ceaa647")));
        assertThat(n.get(NodeHeader.SHA1), is(equalTo("804d716fc5844f1cc5516c8f0be7a480517fdea2")));
    }

    @Test
    public void nested_directories() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        {
            Node n = tok.tellMeAbout(1, "test");
            assertThat(n.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(n.get(NodeHeader.PATH), is(equalTo("test")));
        }{
            Node n = tok.tellMeAbout(1, "test/file1.txt");
            assertThat(n.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(n.get(NodeHeader.PATH), is(equalTo("test/file1.txt")));
            assertThat(n.get(NodeHeader.MD5), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
            assertThat(n.get(NodeHeader.SHA1), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
        }{
            Node n = tok.tellMeAbout(1, "test/file2.txt");
            assertThat(n.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(n.get(NodeHeader.PATH), is(equalTo("test/file2.txt")));
            assertThat(n.get(NodeHeader.MD5), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
            assertThat(n.get(NodeHeader.SHA1), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
        }{
            Node n = tok.tellMeAbout(1, "test/innerdir/file3.txt");
            assertThat(n.get(NodeHeader.ACTION), is(equalTo("add")));
            assertThat(n.get(NodeHeader.PATH), is(equalTo("test/innerdir/file3.txt")));
            assertThat(n.get(NodeHeader.MD5), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
            assertThat(n.get(NodeHeader.SHA1), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
        }
    }

    @Test
    public void directory_deletes() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        assertNull(tok.tellMeAbout(2, "test"));
    }

    @Test
    public void tracks_across_copies() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        assertNotNull(tok.tellMeAbout(2, "test-renamed"));
        assertNotNull(tok.tellMeAbout(2, "test-renamed/innerdir"));
        assertNotNull(tok.tellMeAbout(2, "test-renamed/innerdir/file3.txt"));

        Node file3 = tok.tellMeAbout(2, "test-renamed/innerdir/file3.txt");
        assertThat(file3.get(NodeHeader.MD5), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
        assertThat(file3.get(NodeHeader.SHA1), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
    }

    @Test
    public void tracks_deletes_across_copies() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        assertNull(tok.tellMeAbout(3, "test-renamed/innerdir/file3.txt"));
    }

    @Test
    public void tracks_change_nodes() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/add_and_change_copy_delete.dump"), tok);

        Node node = tok.tellMeAbout(2, "README.txt");
        assertThat(node.getRevision().get().getNumber(), is(2));
        assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
    }

    @Test
    public void deal_with_property_set() throws ParseException {
        TreeOfKnowledge tok = new TreeOfKnowledge();
        SvnDumpParser.consume(TestUtil.openResource("dumps/set_root_property.dump"), tok);

        assertThat(tok.getRoot().getChildren(t -> true).size(), is(0));
    }
}
