package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class NodeAddTest {
    @Test
    public void simple_add() throws ParseException {
        final Map<SvnNodeHeader, String> headers;
        {
            Map<SvnNodeHeader, String> map = new LinkedHashMap<>();
            map.put(SvnNodeHeader.ACTION, "add");
            map.put(SvnNodeHeader.KIND, "dir");
            map.put(SvnNodeHeader.PATH, "testdir");
            headers = Collections.unmodifiableMap(map);
        }

        String dumpFilePath = "dumps/firstcommit.dump";
        {
            SvnDump dump = SvnDumpFileParserTest.parse(dumpFilePath);

            assertThat(dump.getRevisions().size(), is(2));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        }
        {
            SvnNode newNode = new SvnNodeImpl(null);
            newNode.setHeaders(headers);

            SvnDumpMutator nodeAdd = new NodeAdd(1, newNode);
            SvnDump updatedDump = SvnDumpFileParserTest.consume(dumpFilePath, nodeAdd);

            assertThat(updatedDump.getRevisions().size(), is(2));
            assertThat(updatedDump.getRevisions().get(1).getNodes().size(), is(2));

            SvnNode addedNode = updatedDump.getRevisions().get(1).getNodes().get(0);
            assertThat(addedNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
            assertThat(addedNode.get(SvnNodeHeader.KIND), is(equalTo("dir")));
            assertThat(addedNode.get(SvnNodeHeader.PATH), is(equalTo("testdir")));
        }
    }
}