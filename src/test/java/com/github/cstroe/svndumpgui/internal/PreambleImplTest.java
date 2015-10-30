package com.github.cstroe.svndumpgui.internal;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PreambleImplTest {

    @Test
    public void copy_constructor_should_make_a_deep_copy() {
        PreambleImpl preamble = new PreambleImpl();
        preamble.setUUID("someuuid");

        PreambleImpl preambleCopy = new PreambleImpl(preamble);

        assertThat(preamble.getUUID(), is(equalTo(preambleCopy.getUUID())));

        preamble.setUUID("anotheruuid");
        assertThat(preamble.getUUID(), is(not(equalTo(preambleCopy.getUUID()))));
    }

    @Test
    public void descriptive_toString() {
        {
            PreambleImpl preamble = new PreambleImpl();
            assertThat(preamble.toString(), is(equalTo("PreambleImpl")));
        } {
            PreambleImpl preamble = new PreambleImpl("uuid");
            assertThat(preamble.toString(), is(equalTo("PreambleImpl uuid")));
        }
    }
}