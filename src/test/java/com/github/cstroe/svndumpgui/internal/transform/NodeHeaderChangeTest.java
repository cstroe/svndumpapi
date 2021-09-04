package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryMutator;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.hamcrest.MatcherAssert.assertThat;

public class NodeHeaderChangeTest {

    @Test
    public void change_action() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            Node node = dump.getRevisions().get(2).getNodes().get(1);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
            assertNull(node.get(NodeHeader.KIND));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README2.txt")));
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(2, "delete", "README2.txt", NodeHeader.ACTION, "delete", "add");
            Repository updateDump = SvnDumpFileParserTest.consume(dumpFilePath, actionChange);

            assertThat(updateDump.getRevisions().size(), is(3));
            assertThat(updateDump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(updateDump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(updateDump.getRevisions().get(2).getNodes().size(), is(3));

            Node changedNode = updateDump.getRevisions().get(2).getNodes().get(1);
            assertThat(changedNode.get(NodeHeader.ACTION), is(equalTo("add")));
            assertNull(changedNode.get(NodeHeader.KIND));
            assertThat(changedNode.get(NodeHeader.PATH), is(equalTo("README2.txt")));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_not_found() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            Node node = dump.getRevisions().get(2).getNodes().get(1);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
            assertNull(node.get(NodeHeader.KIND));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README2.txt")));
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(2, "add", "README2.txt", NodeHeader.ACTION, "delete", "add");
            SvnDumpFileParserTest.consume(dumpFilePath, actionChange);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_when_no_node_matched() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            Node node = dump.getRevisions().get(2).getNodes().get(1);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
            assertNull(node.get(NodeHeader.KIND));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README2.txt")));
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(1, "delete", "README2.txt", NodeHeader.ACTION, "delete", "add");
            SvnDumpFileParserTest.consume(dumpFilePath, actionChange);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void throws_exception_on_missing_revision() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            Node node = dump.getRevisions().get(2).getNodes().get(1);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
            assertNull(node.get(NodeHeader.KIND));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README2.txt")));
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(4, "add", "README2.txt", NodeHeader.ACTION, "delete", "add");
            SvnDumpFileParserTest.consume(dumpFilePath, actionChange);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_null_old_value() throws ParseException {
        new NodeHeaderChange(4, "add", "README2.txt", NodeHeader.ACTION, null, "add");
    }

    @Test
    public void should_respect_revision_number() throws ParseException {
        String dumpFilePath = "dumps/add_and_multiple_change.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(5));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(3).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(4).getNodes().size(), is(1));
            {
                Node node = dump.getRevisions().get(1).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("add")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = dump.getRevisions().get(2).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = dump.getRevisions().get(3).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = dump.getRevisions().get(4).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(4, "change", "file1.txt", NodeHeader.ACTION, "change", "delete");
            Repository updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, actionChange);

            assertThat(updatedDump.getRevisions().size(), is(5));
            assertThat(updatedDump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(updatedDump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(updatedDump.getRevisions().get(3).getNodes().size(), is(1));
            assertThat(updatedDump.getRevisions().get(4).getNodes().size(), is(1));
            {
                Node node = updatedDump.getRevisions().get(1).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("add")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = updatedDump.getRevisions().get(2).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = updatedDump.getRevisions().get(3).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = updatedDump.getRevisions().get(4).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }
        }
    }

    @Test
    public void should_respect_revision_number_earlier() throws ParseException {
        String dumpFilePath = "dumps/add_and_multiple_change.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(5));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(3).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(4).getNodes().size(), is(1));
            {
                Node node = dump.getRevisions().get(1).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("add")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
            Node node = dump.getRevisions().get(2).getNodes().get(0);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
            assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
        }{
            Node node = dump.getRevisions().get(3).getNodes().get(0);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
            assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
        }{
            Node node = dump.getRevisions().get(4).getNodes().get(0);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
            assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
        }
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(2, "change", "file1.txt", NodeHeader.ACTION, "change", "delete");
            Repository updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, actionChange);

            assertThat(updatedDump.getRevisions().size(), is(5));
            assertThat(updatedDump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(updatedDump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(updatedDump.getRevisions().get(3).getNodes().size(), is(1));
            assertThat(updatedDump.getRevisions().get(4).getNodes().size(), is(1));
            {
                Node node = updatedDump.getRevisions().get(1).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("add")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = updatedDump.getRevisions().get(2).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = updatedDump.getRevisions().get(3).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }{
                Node node = updatedDump.getRevisions().get(4).getNodes().get(0);
                assertThat(node.get(NodeHeader.ACTION), is(equalTo("change")));
                assertThat(node.get(NodeHeader.KIND), is(equalTo("file")));
                assertThat(node.get(NodeHeader.PATH), is(equalTo("file1.txt")));
            }
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void old_value_does_not_match() throws ParseException {
        String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            Repository dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            Node node = dump.getRevisions().get(2).getNodes().get(1);
            assertThat(node.get(NodeHeader.ACTION), is(equalTo("delete")));
            assertNull(node.get(NodeHeader.KIND));
            assertThat(node.get(NodeHeader.PATH), is(equalTo("README2.txt")));
        }{
            RepositoryMutator actionChange = new NodeHeaderChange(2, "delete", "README2.txt", NodeHeader.PATH, "README1.txt", "README3.txt");
            SvnDumpFileParserTest.consume(dumpFilePath, actionChange);
        }
    }
}