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

// TODO: Remove this class!
public class ConsumerChainTest {

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


        SvnDumpMutator nodeRemove = new NodeRemove(2, "delete", "README2.txt");

        dump.getRevisions().stream().forEach(nodeRemove::consume);

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(2)); // node cleared
        SvnNode firstNode = dump.getRevisions().get(2).getNodes().get(0);
        SvnNode secondNode = dump.getRevisions().get(2).getNodes().get(1);

        assertThat(firstNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));
        assertThat(secondNode.get(SvnNodeHeader.PATH), is(not(equalTo("README2.txt"))));

    }

    @Test
    public void chain_is_valid_if_all_validators_are_valid() {

//        SvnDumpValidator validatorChain = new TrueValidator();
//        validatorChain.tail().continueTo(new TrueValidator());
//        validatorChain.tail().continueTo(new TrueValidator());
//        validatorChain.tail().continueTo(new TrueValidator());
//
//        assertTrue(validatorChain.isValid());
    }

    @Test
    public void chain_is_invalid_if_one_validator_is_invalid() {
//        Mockery context = new Mockery();
//
//        SvnDumpValidator v1 = context.mock(SvnDumpValidator.class, "v1");
//        SvnDumpValidator v2 = context.mock(SvnDumpValidator.class, "v2");
//        SvnDumpValidator v3 = context.mock(SvnDumpValidator.class, "v3");
//        SvnDumpValidator v4 = context.mock(SvnDumpValidator.class, "v4");
//
//        context.checking(new Expectations() {{
//            allowing(v1).isValid(); will(returnValue(true));
//            allowing(v2).isValid(); will(returnValue(true));
//            allowing(v3).isValid(); will(returnValue(false));
//            allowing(v4).isValid(); will(returnValue(true));
//        }});
//
//        ConsumerChain chain = new ConsumerChain();
//        chain.add(v1);
//        chain.add(v2);
//        chain.add(v3);
//        chain.add(v4);
//
//        assertFalse(chain.isValid());
    }
    
    @Test
    public void chain_respects_early_termination_of_nodes() {
//        Mockery context = new Mockery();
//
//        SvnNode n1 = context.mock(SvnNode.class, "n1");
//        SvnDumpMutator m1 = context.mock(SvnDumpMutator.class, "m1");
//        SvnDumpValidator v1 = context.mock(SvnDumpValidator.class, "v1");
//
//        context.checking(new Expectations() {{
//            allowing(m1).consume(n1); will(returnValue(false));
//        }});
//
//        ConsumerChain chain = new ConsumerChain();
//        chain.add(m1);
//        chain.add(v1);
//
//        chain.consume(n1);
    }

}