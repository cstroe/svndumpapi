package com.github.cstroe.svndumpgui.internal.transform.property;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;

public class EnsureTrailingLinefeedTest {
    @Test
    public void add_line_feed() {
        String result = new EnsureTrailingLinefeed().apply("thisissomestring");
        assertThat(result, is(equalTo("thisissomestring\n")));
    }

    @Test
    public void does_not_add_duplicate_line_feed() {
        String result = new EnsureTrailingLinefeed().apply("thisissomestring\n");
        assertThat(result, is(equalTo("thisissomestring\n")));
    }

    @Test
    public void works_with_empty_string() {
        String result = new EnsureTrailingLinefeed().apply("");
        assertThat(result, is(equalTo("\n")));
    }
}