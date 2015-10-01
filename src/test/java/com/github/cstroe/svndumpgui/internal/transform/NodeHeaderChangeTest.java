package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class NodeHeaderChangeTest {

    @Test
    public void change_action() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        SvnNode node = dump.getRevisions().get(2).getNodes().get(1);
        assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
        assertNull(node.get(SvnNodeHeader.KIND));
        assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README2.txt")));

        SvnDumpMutator actionChange = new NodeHeaderChange(2, "delete", "README2.txt", SvnNodeHeader.ACTION, "delete", "add");
        SvnDump updateDump = SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", actionChange);

        assertThat(updateDump.getRevisions().size(), is(3));
        assertThat(updateDump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(updateDump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(updateDump.getRevisions().get(2).getNodes().size(), is(3));

        SvnNode changedNode = updateDump.getRevisions().get(2).getNodes().get(1);
        assertThat(changedNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertNull(changedNode.get(SvnNodeHeader.KIND));
        assertThat(changedNode.get(SvnNodeHeader.PATH), is(equalTo("README2.txt")));
    }

}