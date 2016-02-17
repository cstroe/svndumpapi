package com.github.cstroe.svndumpgui.internal.consumer;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.api.TreeOfKnowledge;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.utility.range.MultiSpan;
import com.github.cstroe.svndumpgui.internal.utility.tree.CLTreeNode;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class TreeOfKnowledgeImplTest {
    @Test
    public void empty() throws ParseException {
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
        SvnDumpParser.consume(TestUtil.openResource("dumps/empty.dump"), tok);

        CLTreeNode<Triplet<MultiSpan, String, Node>> root = tok.getRoot();
        assertNotNull(root);

        List<CLTreeNode<Triplet<MultiSpan, String, Node>>> children = root.getChildren(n -> true);
        assertThat(children.size(), is(0));
    }

    @Test
    public void add_file() throws ParseException {
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
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
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
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
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        assertNull(tok.tellMeAbout(2, "test"));
    }

    @Test
    public void tracks_across_copies() throws ParseException {
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
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
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
        SvnDumpParser.consume(TestUtil.openResource("dumps/inner_dir.dump"), tok);
        assertNull(tok.tellMeAbout(3, "test-renamed/innerdir/file3.txt"));
    }

    @Test
    public void tracks_change_nodes() throws ParseException {
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
        SvnDumpParser.consume(TestUtil.openResource("dumps/add_and_change_copy_delete.dump"), tok);

        Node node = tok.tellMeAbout(2, "README.txt");
        assertThat(node.getRevision().get().getNumber(), is(2));
        assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
    }

    @Test
    public void deal_with_property_set() throws ParseException {
        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
        SvnDumpParser.consume(TestUtil.openResource("dumps/set_root_property.dump"), tok);

        assertThat(tok.getRoot().getChildren(t -> true).size(), is(0));
    }

    @Test
    public void consume_all_the_files() throws ParseException {
        consume("dumps/simple_branch_and_merge_renamed.dump");
        consume("dumps/svn_copy_file_many_times_new_content.dump");
        consume("dumps/property_change_on_file.dump");
        consume("dumps/inner_dir.dump");
        consume("dumps/set_root_property.dump");
        consume("dumps/empty.dump");
        consume("dumps/add_edit_delete_add.dump");
        consume("dumps/add_and_copychange_once.dump");
        consume("dumps/svn_copy_and_delete.before.dump");
        consume("dumps/svn_multi_dir_delete.dump");
        consume("dumps/many_branches_renamed.dump");
        consume("dumps/add_and_copychange.dump");
        consume("dumps/svn_rename_no_copy_hashes.dump");
        consume("dumps/add_file_in_directory.before.dump");
        consume("dumps/extra_newline_in_log_message.dump");
        consume("dumps/composite_commit.dump");
        consume("dumps/svn_add_directory.dump");
        consume("dumps/add_file.dump");
        consume("dumps/svn_delete_with_add.dump");
        consume("dumps/svn_copy_file.dump");
        consume("dumps/add_and_multiple_change.dump");
        consume("dumps/svn_multi_file_delete.dump");
        consume("dumps/svn_copy_and_delete.after.dump");
        consume("dumps/utf8_log_message.dump");
        consume("dumps/firstcommit.dump");
        consume("dumps/svn_multi_file_delete_multiple_authors.dump");
        consume("dumps/svn_replace.dump");
        consume("dumps/undelete.dump");
        consume("dumps/property_change_on_root.dump");
        consume("dumps/svn_copy_file_new_content.dump");
        consume("dumps/binary_commit.dump");
        consume("dumps/simple_branch_and_merge.dump");
        consume("dumps/add_file_no_node_properties.dump");
        consume("dumps/invalid/composite_commit.dump");
        consume("dumps/svn_copy_file_many_times.dump");
        consume("dumps/add_and_change_copy_delete.dump");
        consume("dumps/svn_delete_file.dump");
        consume("dumps/svn_rename.dump");
        consume("dumps/many_branches.dump");
        consume("dumps/different_node_order2.dump");
        consume("dumps/different_node_order.dump");
        consume("dumps/add_file_in_directory.after.dump");
    }

    private void consume(String file) throws ParseException {
        RepositoryInMemory memoryCopy = new RepositoryInMemory();
        SvnDumpParser.consume(TestUtil.openResource(file), memoryCopy);

        List<Pair<Integer, String>> nodes = getNodeList(memoryCopy.getRepo());

        TreeOfKnowledgeImpl tok = new TreeOfKnowledgeImpl();
        SvnDumpParser.consume(TestUtil.openResource(file), tok);

        for(Pair<Integer, String> pair : nodes) {
            Node node = tok.tellMeAbout(pair.getValue0(), pair.getValue1());
            assertNotNull(node);
            assertThat(node.getRevision().get().getNumber(), is(pair.getValue0()));
            assertThat(node.get(NodeHeader.PATH), is(equalTo((pair.getValue1()))));
        }
    }

    private List<Pair<Integer, String>> getNodeList(Repository repo) {
        List<Pair<Integer, String>> nodes = new ArrayList<>();

        for(Revision revision : repo.getRevisions()) {
            for(Node node : revision.getNodes()) {
                if("delete".equals(node.get(NodeHeader.ACTION))) {
                    continue;
                }

                final String path = node.get(NodeHeader.PATH);
                if(path == null || path.isEmpty()) {
                    continue;
                }

                final String textContentLength = node.get(NodeHeader.TEXT_CONTENT_LENGTH);
                if(textContentLength == null) {
                    continue;
                }

                nodes.add(Pair.with(node.getRevision().get().getNumber(), node.get(NodeHeader.PATH)));
            }
        }

        return nodes;
    }

    @Test(expected = IllegalArgumentException.class)
    public void error_on_unknown_node_action() {
        TreeOfKnowledge tok = new TreeOfKnowledgeImpl();
        Node unknownNode = new NodeImpl(new RevisionImpl(1));
        unknownNode.getHeaders().put(NodeHeader.ACTION, "unknown");
        tok.consume(unknownNode);
    }
}
