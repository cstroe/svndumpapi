package com.github.cstroe.svndumpgui.internal.writer.git;

import org.junit.Test;

import static org.junit.Assert.*;

public class GitWriterTest {

    @Test
    public void cleanPath() {
        assertEquals("maindir/some/file", GitWriter.cleanPath("maindir/trunk/some/file"));
        assertEquals("maindir/some/file", GitWriter.cleanPath("maindir/branches/b2/some/file"));
    }


}