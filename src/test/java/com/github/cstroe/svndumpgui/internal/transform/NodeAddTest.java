package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class NodeAddTest {
    @Test
    public void simple_add() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/firstcommit.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        Map<SvnNodeHeader, String> headers = new LinkedHashMap<>();
        headers.put(SvnNodeHeader.ACTION, "add");
        headers.put(SvnNodeHeader.KIND, "dir");
        headers.put(SvnNodeHeader.PATH, "testdir");

        SvnNode newNode = new SvnNodeImpl(null);
        newNode.setHeaders(headers);

        SvnDumpMutator nodeAdd = new NodeAdd(1, newNode);
        SvnDump updatedDump = SvnDumpFileParserTest.consume("dumps/firstcommit.dump", nodeAdd);

        assertThat(updatedDump.getRevisions().size(), is(2));
        assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(2));

        SvnNode addedNode = updatedDump.getRevisions().get(1).getNodes().get(0);
        assertThat(addedNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(addedNode.get(SvnNodeHeader.KIND), is(equalTo("dir")));
        assertThat(addedNode.get(SvnNodeHeader.PATH), is(equalTo("testdir")));
    }
}