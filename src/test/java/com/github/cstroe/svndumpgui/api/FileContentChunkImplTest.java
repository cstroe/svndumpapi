package com.github.cstroe.svndumpgui.api;

import com.github.cstroe.svndumpgui.internal.FileContentChunkImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FileContentChunkImplTest {
    @Test
    public void copy_constructor_should_make_deep_copy() {
        byte[] content = new byte[5];
        content[0] = 'g';
        content[1] = 'r';
        content[2] = 'e';
        content[3] = 'a';
        content[4] = 't';

        FileContentChunk chunk = new FileContentChunkImpl(content);
        FileContentChunk chunkCopy = new FileContentChunkImpl(chunk);

        content[0] = 'X';
        content[1] = 'X';
        content[2] = 'X';
        content[3] = 'X';
        content[4] = 'X';

        assertThat(new String(chunkCopy.getContent()), is(equalTo("great")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_allow_null_content() {
        new FileContentChunkImpl((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void copy_constructor_with_null_content() {
        FileContentChunk badChunk = new FileContentChunk() {
            @Override
            public byte[] getContent() {
                return null;
            }

            @Override
            public void setContent(byte[] content) {}
        };

        new FileContentChunkImpl(badChunk);
    }

    @Test
    public void set_content_does_what_it_should_do() {
        byte[] byteArray = new byte[5];
        byteArray[0] = 'A';
        byteArray[0] = 'B';
        byteArray[0] = 'C';
        byteArray[0] = 'D';

        byte[] byteArray2 = new byte[4];
        byteArray2[0] = 'a';
        byteArray2[1] = 'b';
        byteArray2[2] = 'c';
        byteArray2[3] = 'd';

        FileContentChunk chunk = new FileContentChunkImpl(byteArray);
        assertThat(chunk.getContent().length, is(5));
        assertThat(chunk.getContent(), is(equalTo(byteArray)));

        chunk.setContent(byteArray2);
        assertThat(chunk.getContent().length, is(4));
        assertThat(chunk.getContent(), is(equalTo(byteArray2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_let_anyone_set_null_content() {
        byte[] byteArray = new byte[16];

        FileContentChunk chunk = new FileContentChunkImpl(byteArray);
        chunk.setContent(null);
    }

    @Test
    public void toString_should_describe_the_chunk() {
        assertThat(new FileContentChunkImpl(new byte[0]).toString(), is(equalTo("0 bytes")));
        assertThat(new FileContentChunkImpl(new byte[5]).toString(), is(equalTo("5 bytes")));
        assertThat(new FileContentChunkImpl(new byte[999]).toString(), is(equalTo("999 bytes")));
    }
}