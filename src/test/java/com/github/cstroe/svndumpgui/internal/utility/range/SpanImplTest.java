package com.github.cstroe.svndumpgui.internal.utility.range;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SpanImplTest {
    @Test
    public void simplest_range() {
        Span span = new SpanImpl(1,1);
        assertFalse(span.contains(0));
        assertTrue(span.contains(1));
        assertFalse(span.contains(2));
    }

    @Test
    public void simple_range() {
        Span span = new SpanImpl(0, 1);
        assertThat(span.low(), is(0));
        assertThat(span.high(), is(1));
        assertTrue(span.contains(0));
        assertTrue(span.contains(1));
        assertFalse(span.contains(-1));
        assertFalse(span.contains(2));
    }

    @Test
    public void negative_infinity() {
        Span span = new SpanImpl(Span.NEGATIVE_INFINITY, 10);
        assertTrue(span.contains(9));
        assertTrue(span.contains(10));
        assertFalse(span.contains(11));
        assertTrue(span.contains(-1234565));
        assertFalse(span.contains(1234));
    }

    @Test
    public void positive_infinity() {
        Span span = new SpanImpl(2, Span.POSITIVE_INFINITY);
        assertFalse(span.contains(1));
        assertTrue(span.contains(2));
        assertTrue(span.contains(3));
        assertFalse(span.contains(-1234565));
        assertTrue(span.contains(1234));
    }

    @Test
    public void all_numbers() {
        Span span = new SpanImpl(Span.NEGATIVE_INFINITY, Span.POSITIVE_INFINITY);
        assertTrue(span.contains(0));
        assertTrue(span.contains(1));
        assertTrue(span.contains(1000000));
        assertTrue(span.contains(-1));
        assertTrue(span.contains(-999999));
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_1() {
        new SpanImpl(1,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_2() {
        new SpanImpl(Span.POSITIVE_INFINITY,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_3() {
        new SpanImpl(Span.POSITIVE_INFINITY,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_4() {
        new SpanImpl(-10000000,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_infinity_does_not_specify_a_span() {
        new SpanImpl(Span.NEGATIVE_INFINITY,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void positive_infinity_does_not_specify_a_span() {
        new SpanImpl(Span.POSITIVE_INFINITY,Span.POSITIVE_INFINITY);
    }
}