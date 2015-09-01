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
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

public class MutatorChainTest {

    @Test
    public void mutator_chain_with_single_mutator() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        SvnNode node = dump.getRevisions().get(2).getNodes().get(1);
        assertThat(node.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
        assertNull(node.get(SvnNodeHeader.KIND));
        assertThat(node.get(SvnNodeHeader.PATH), is(equalTo("README2.txt")));

        MutatorChain chain = new MutatorChain();

        SvnDumpMutator nodeRemove = new NodeRemove(2, "delete", "README2.txt");
        chain.add(nodeRemove);

        chain.mutate(dump);

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(2)); // node cleared
        SvnNode firstNode = dump.getRevisions().get(2).getNodes().get(0);
        SvnNode secondNode = dump.getRevisions().get(2).getNodes().get(1);

        assertThat(firstNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));
        assertThat(secondNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));

    }

}