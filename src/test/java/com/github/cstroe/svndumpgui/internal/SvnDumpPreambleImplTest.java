package com.github.cstroe.svndumpgui.internal;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnDumpPreambleImplTest {

    @Test
    public void copy_constructor_should_make_a_deep_copy() {
        SvnDumpPreambleImpl preamble = new SvnDumpPreambleImpl();
        preamble.setUUID("someuuid");

        SvnDumpPreambleImpl preambleCopy = new SvnDumpPreambleImpl(preamble);

        assertThat(preamble.getUUID(), is(equalTo(preambleCopy.getUUID())));

        preamble.setUUID("anotheruuid");
        assertThat(preamble.getUUID(), is(not(equalTo(preambleCopy.getUUID()))));
    }

}