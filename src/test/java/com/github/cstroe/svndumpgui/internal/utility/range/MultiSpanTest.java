package com.github.cstroe.svndumpgui.internal.utility.range;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiSpanTest {
    @Test
    public void one_span()  {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        assertTrue(multiSpan.contains(0));
        assertTrue(multiSpan.contains(1));
        assertFalse(multiSpan.contains(-1));
        assertFalse(multiSpan.contains(2));
        assertFalse(multiSpan.contains(-10));
        assertFalse(multiSpan.contains(12));
    }

    @Test
    public void two_spans() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        multiSpan.add(new SpanImpl(3, 4));
        assertFalse(multiSpan.contains(-20000));
        assertFalse(multiSpan.contains(-2));
        assertFalse(multiSpan.contains(-1));
        assertTrue(multiSpan.contains(0));
        assertTrue(multiSpan.contains(1));
        assertFalse(multiSpan.contains(2));
        assertTrue(multiSpan.contains(3));
        assertTrue(multiSpan.contains(4));
        assertFalse(multiSpan.contains(5));
        assertFalse(multiSpan.contains(6));
        assertFalse(multiSpan.contains(6000000));
    }

    @Test
    public void three_overlapping_spans() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        multiSpan.add(new SpanImpl(3, 4));
        multiSpan.add(new SpanImpl(1, 2));
        assertFalse(multiSpan.contains(-20000));
        assertFalse(multiSpan.contains(-2));
        assertFalse(multiSpan.contains(-1));
        assertTrue(multiSpan.contains(0));
        assertTrue(multiSpan.contains(1));
        assertTrue(multiSpan.contains(2));
        assertTrue(multiSpan.contains(3));
        assertTrue(multiSpan.contains(4));
        assertFalse(multiSpan.contains(5));
        assertFalse(multiSpan.contains(6));
        assertFalse(multiSpan.contains(6000000));
    }

    @Test
    public void span_merging() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        assertThat(multiSpan.getSpans().size(), is(1));
        multiSpan.add(new SpanImpl(1, 2));
        assertThat(multiSpan.getSpans().size(), is(1));

        assertFalse(multiSpan.contains(-1));
        assertTrue(multiSpan.contains(0));
        assertTrue(multiSpan.contains(1));
        assertTrue(multiSpan.contains(2));
        assertFalse(multiSpan.contains(3));
    }

    @Test
    public void span_merge_reduction() {
        MultiSpan multiSpan = new MultiSpan();
        multiSpan.add(new SpanImpl(0, 1));
        assertThat(multiSpan.getSpans().size(), is(1));
        multiSpan.add(new SpanImpl(2, 3));
        assertThat(multiSpan.getSpans().size(), is(2));
        multiSpan.add(new SpanImpl(1, 2));
        assertThat(multiSpan.getSpans().size(), is(1));

        assertFalse(multiSpan.contains(-1));
        assertTrue(multiSpan.contains(0));
        assertTrue(multiSpan.contains(1));
        assertTrue(multiSpan.contains(2));
        assertTrue(multiSpan.contains(3));
        assertFalse(multiSpan.contains(4));
    }

    @Test
    public void cutoff() {
        {
            MultiSpan multiSpan = new MultiSpan();
            multiSpan.add(new SpanImpl(0, 10));
            multiSpan.cutoff(5);
            assertTrue(multiSpan.contains(5));
            assertFalse(multiSpan.contains(6));
        }{
            MultiSpan multiSpan = new MultiSpan();
            multiSpan.add(new SpanImpl(6, 10));
            multiSpan.add(new SpanImpl(0, 4));
            multiSpan.cutoff(7);
            assertTrue(multiSpan.contains(4));
            assertFalse(multiSpan.contains(5));
            assertTrue(multiSpan.contains(6));
            assertTrue(multiSpan.contains(7));
            assertFalse(multiSpan.contains(8));
        }
    }
}