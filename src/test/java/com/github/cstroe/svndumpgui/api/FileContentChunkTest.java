package com.github.cstroe.svndumpgui.api;

import com.github.cstroe.svndumpgui.internal.FileContentChunkImpl;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class FileContentChunkTest {
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
}