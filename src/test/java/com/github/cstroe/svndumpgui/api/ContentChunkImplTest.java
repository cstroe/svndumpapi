package com.github.cstroe.svndumpgui.api;

import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class ContentChunkImplTest {
    @Test
    public void copy_constructor_should_make_deep_copy() {
        byte[] content = new byte[5];
        content[0] = 'g';
        content[1] = 'r';
        content[2] = 'e';
        content[3] = 'a';
        content[4] = 't';

        ContentChunk chunk = new ContentChunkImpl(content);
        ContentChunk chunkCopy = new ContentChunkImpl(chunk);

        content[0] = 'X';
        content[1] = 'X';
        content[2] = 'X';
        content[3] = 'X';
        content[4] = 'X';

        assertThat(new String(chunkCopy.getContent()), is(equalTo("great")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_allow_null_content() {
        new ContentChunkImpl((byte[])null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void copy_constructor_with_null_content() {
        ContentChunk badChunk = new ContentChunk() {
            @Override
            public byte[] getContent() {
                return null;
            }

            @Override
            public void setContent(byte[] content) {}
        };

        new ContentChunkImpl(badChunk);
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

        ContentChunk chunk = new ContentChunkImpl(byteArray);
        assertThat(chunk.getContent().length, is(5));
        assertThat(chunk.getContent(), is(equalTo(byteArray)));

        chunk.setContent(byteArray2);
        assertThat(chunk.getContent().length, is(4));
        assertThat(chunk.getContent(), is(equalTo(byteArray2)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_let_anyone_set_null_content() {
        byte[] byteArray = new byte[16];

        ContentChunk chunk = new ContentChunkImpl(byteArray);
        chunk.setContent(null);
    }

    @Test
    public void toString_should_describe_the_chunk() {
        assertThat(new ContentChunkImpl(new byte[0]).toString(), is(equalTo("0 bytes")));
        assertThat(new ContentChunkImpl(new byte[5]).toString(), is(equalTo("5 bytes")));
        assertThat(new ContentChunkImpl(new byte[999]).toString(), is(equalTo("999 bytes")));
    }
}