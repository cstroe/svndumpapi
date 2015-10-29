package com.github.cstroe.svndumpgui.internal;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class SimplePrintStreamTest {
    @Test(expected = IllegalArgumentException.class)
    public void null_output_stream() {
        new SimplePrintStream(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void null_line_separator() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new SimplePrintStream(baos, null);
    }

    @Test
    public void custom_line_separator() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SimplePrintStream sps = new SimplePrintStream(baos, "");

        sps.println("test");
        sps.println(1234);

        assertThat(baos.toString(), is(equalTo("test1234")));
    }

    @Test(expected = RuntimeException.class)
    public void flush_throws_exception() {
        OutputStream badStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {}

            @Override
            public void flush() throws IOException {
                throw new IOException("bad stream");
            }
        };

        SimplePrintStream sps = new SimplePrintStream(badStream, "");
        sps.flush();
    }
}