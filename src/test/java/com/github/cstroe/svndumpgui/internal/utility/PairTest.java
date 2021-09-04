package com.github.cstroe.svndumpgui.internal.utility;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class PairTest {
    @Test
    public void test_toString() {
        String left = "left";
        String right = "right";
        assertThat(Pair.of(left, right).toString(), is(equalTo("(left, right)")));
    }

    @Test
    public void test_equals() {
        String one = "one";
        String two = "two";

        assertThat(Pair.of(one, two), is(equalTo(Pair.of(one, two))));
        assertThat(Pair.of(one, two), is(not(equalTo(Pair.of(two, one)))));

        Pair<String, String> aPair = Pair.of(one, two);
        assertEquals(aPair, aPair);

        assertNotEquals(aPair, one);

        assertThat(Pair.of(null, two), is(equalTo(Pair.of(null, two))));
        assertThat(Pair.of(one, null), is(equalTo(Pair.of(one, null))));
        assertThat(Pair.of(null, null), is(equalTo(Pair.of(null, null))));
        assertThat(Pair.of(one, two), is(not((equalTo(Pair.of(one, null))))));
        assertThat(Pair.of(one, null), is(not((equalTo(Pair.of(one, two))))));

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        Object o1 = new Object() {
            @Override
            public boolean equals(Object obj) {
                return true;
            }
        };

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        Object o2 = new Object() {
            @Override
            public boolean equals(Object obj) {
                return true;
            }
        };


        assertThat(Pair.of(one, o1), is((equalTo(Pair.of(one, o2)))));
    }

    @Test
    public void test_hashcode() {
        String one = "one";
        String two = "two";

        assertThat(Pair.of(one, two).hashCode(), is(3530918));
        assertThat(Pair.of(null, two).hashCode(), is(two.hashCode()));
        assertThat(Pair.of(one, null).hashCode(), is(31 * one.hashCode()));
    }
}