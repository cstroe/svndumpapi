package com.github.cstroe.svndumpgui.internal.transform.property;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Dos2UnixTest {
    @Test
    public void add_line_feed() {
        String result = new Dos2Unix().apply("thisissomestring");
        assertThat(result, is(equalTo("thisissomestring\n")));
    }

    @Test
    public void does_not_add_duplicate_line_feed() {
        String result = new Dos2Unix().apply("thisissomestring\n");
        assertThat(result, is(equalTo("thisissomestring\n")));
    }

    @Test
    public void works_with_empty_string() {
        String result = new Dos2Unix().apply("");
        assertThat(result, is(equalTo("\n")));
    }

    @Test
    public void convert_windows_line_feeds_to_unix_style() {
        {
            final String input = "this is some text\r\nthis is more text\n";
            final String output = "this is some text\nthis is more text\n";
            assertThat(new Dos2Unix().apply(input), is(equalTo(output)));
        }{
            final String input = "this is some text\r\nthis is more text\r\n";
            final String output = "this is some text\nthis is more text\n";
            assertThat(new Dos2Unix().apply(input), is(equalTo(output)));
        }{
            final String input = "this is some text\nthis is more text\r\n";
            final String output = "this is some text\nthis is more text\n";
            assertThat(new Dos2Unix().apply(input), is(equalTo(output)));
        }{
            final String input = "\r\n";
            final String output = "\n";
            assertThat(new Dos2Unix().apply(input), is(equalTo(output)));
        }{
            final String input = "this is some text\r\n";
            final String output = "this is some text\n";
            assertThat(new Dos2Unix().apply(input), is(equalTo(output)));
        }
    }
}