package com.github.cstroe.svndumpgui.internal.writer;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AbstractSvnDumpWriterTest {
    @Test
    public void normal_operation() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        AbstractSvnDumpWriter writer = new AbstractSvnDumpWriter() {};
        writer.writeTo(os);
        writer.ps().print("this is a string");
        writer.ps().println();
        writer.ps().print(1234);
        writer.ps().println();
        writer.ps().println("abcd");
        writer.ps().println(4567);

        final String lineSeparator = System.getProperty("line.separator");
        final String expected = "this is a string" + lineSeparator +
                "1234" + lineSeparator + "abcd" + lineSeparator + "4567" + lineSeparator;

        assertThat(os.toString(), is(equalTo(expected)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_allow_null_stream() {
        AbstractSvnDumpWriter writer = new AbstractSvnDumpWriter() {};
        writer.writeTo(null);
    }

    @Test(expected = RuntimeException.class)
    public void dont_swallow_exception() {
        OutputStream badStream = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("bad stream");
            }
        };

        AbstractSvnDumpWriter writer = new AbstractSvnDumpWriter() {};
        writer.writeTo(badStream);
        writer.ps().print("test");
    }
}