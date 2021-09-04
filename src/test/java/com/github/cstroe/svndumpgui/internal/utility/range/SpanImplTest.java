package com.github.cstroe.svndumpgui.internal.utility.range;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class SpanImplTest {

    private Span span(int low, int high)  {
        return new SpanImpl(low, high);
    }

    @Test
    public void simplest_range() {
        Span span = span(1,1);
        assertFalse(span.contains(0));
        assertTrue(span.contains(1));
        assertFalse(span.contains(2));
    }

    @Test
    public void simple_range() {
        Span span = span(0, 1);
        assertThat(span.low(), is(0));
        assertThat(span.high(), is(1));
        assertTrue(span.contains(0));
        assertTrue(span.contains(1));
        assertFalse(span.contains(-1));
        assertFalse(span.contains(2));
    }

    @Test
    public void negative_infinity() {
        Span span = span(Span.NEGATIVE_INFINITY, 10);
        assertTrue(span.contains(9));
        assertTrue(span.contains(10));
        assertFalse(span.contains(11));
        assertTrue(span.contains(-1234565));
        assertFalse(span.contains(1234));
    }

    @Test
    public void positive_infinity() {
        Span span = span(2, Span.POSITIVE_INFINITY);
        assertFalse(span.contains(1));
        assertTrue(span.contains(2));
        assertTrue(span.contains(3));
        assertFalse(span.contains(-1234565));
        assertTrue(span.contains(1234));
    }

    @Test
    public void all_numbers() {
        Span span = span(Span.NEGATIVE_INFINITY, Span.POSITIVE_INFINITY);
        assertTrue(span.contains(0));
        assertTrue(span.contains(1));
        assertTrue(span.contains(1000000));
        assertTrue(span.contains(-1));
        assertTrue(span.contains(-999999));
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_1() {
        span(1,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_2() {
        span(Span.POSITIVE_INFINITY,0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_3() {
        span(Span.POSITIVE_INFINITY,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void does_not_allow_invalid_parameters_4() {
        span(-10000000,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negative_infinity_does_not_specify_a_span() {
        span(Span.NEGATIVE_INFINITY,Span.NEGATIVE_INFINITY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void positive_infinity_does_not_specify_a_span() {
        span(Span.POSITIVE_INFINITY,Span.POSITIVE_INFINITY);
    }

    @Test
    public void overlapping() {
        {
            Span s1 = span(0,1);
            assertTrue(s1.overlaps(s1));
        }{
            Span s1 = span(0,1);
            Span s2 = span(0,1);
            assertTrue(s1.overlaps(s2));
            assertTrue(s2.overlaps(s1));
        }{
            Span s1 = span(0,1);
            Span s2 = span(1,2);
            assertTrue(s1.overlaps(s2));
            assertTrue(s2.overlaps(s1));
        }{
            Span s1 = span(0,1);
            Span s2 = span(2,3);
            assertFalse(s1.overlaps(s2));
            assertFalse(s2.overlaps(s1));
        }{
            Span s1 = span(0,Span.POSITIVE_INFINITY);
            Span s2 = span(1,2);
            assertTrue(s1.overlaps(s2));
            assertTrue(s2.overlaps(s1));
        }{
            Span s1 = span(0,Span.POSITIVE_INFINITY);
            Span s2 = span(Span.NEGATIVE_INFINITY,-1);
            assertFalse(s1.overlaps(s2));
            assertFalse(s2.overlaps(s1));
        }{
            Span s1 = span(Span.NEGATIVE_INFINITY,Span.POSITIVE_INFINITY);
            Span s2 = span(1,2);
            assertTrue(s1.overlaps(s2));
            assertTrue(s2.overlaps(s1));
        }{
            Span s1 = span(Span.NEGATIVE_INFINITY,Span.POSITIVE_INFINITY);
            Span s2 = span(Span.NEGATIVE_INFINITY,Span.POSITIVE_INFINITY);
            assertTrue(s1.overlaps(s2));
            assertTrue(s2.overlaps(s1));
        }
    }

    @Test
    public void cutoff() {
        Span s1 = span(3, 20);
        s1.cutoff(15);
        assertEquals(15, s1.high());
    }

    @Test(expected = IllegalArgumentException.class)
    public void cutoff_low_range() {
        Span s1 = span(10, 20);
        s1.cutoff(5);
    }
}
