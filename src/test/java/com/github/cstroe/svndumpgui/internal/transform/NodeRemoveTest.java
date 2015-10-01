package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class NodeRemoveTest {

    @Test
    public void remove_node() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        SvnNode node = dump.getRevisions().get(2).getNodes().get(1);
        assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
        assertNull(node.get(SvnNodeHeader.KIND));
        assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README2.txt")));

        SvnDumpMutator nodeRemove = new NodeRemove(2, "delete", "README2.txt");
        SvnDump updatedDump = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", nodeRemove);

        assertThat(updatedDump.getRevisions().size(), is(3));
        assertThat(updatedDump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(updatedDump.getRevisions().get(2).getNodes().size(), is(2)); // node cleared
        SvnNode firstNode = updatedDump.getRevisions().get(2).getNodes().get(0);
        SvnNode secondNode = updatedDump.getRevisions().get(2).getNodes().get(1);

        assertThat(firstNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));
        assertThat(secondNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));
    }

    @Test
    public void remove_should_respect_the_target_revision_number() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/add_edit_delete_add.dump");

        assertThat(dump.getRevisions().size(), is(5));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        SvnNode node = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(node.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));

        assertThat(dump.getRevisions().get(4).getNodes().size(), is(1));
        node = dump.getRevisions().get(4).getNodes().get(0);
        assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(node.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));

        SvnDumpMutator nodeRemove = new NodeRemove(4, "add", "README.txt");
        SvnDump updatedDump = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", nodeRemove);

        assertThat(updatedDump.getRevisions().size(), is(5));
        assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(1));
        SvnNode nodeAfter = updatedDump.getRevisions().get(1).getNodes().get(0);
        assertThat(nodeAfter.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(nodeAfter.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(nodeAfter.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(updatedDump.getRevisions().get(4).getNodes().size(), is(0));
    }
}