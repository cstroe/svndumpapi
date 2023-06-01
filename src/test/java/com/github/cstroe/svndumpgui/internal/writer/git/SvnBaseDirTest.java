package com.github.cstroe.svndumpgui.internal.writer.git;

import org.junit.Test;

import static org.junit.Assert.*;

public class SvnBaseDirTest {
    @Test
    public void stripBranchPrefix() {
        SvnBaseDir dir = SvnBaseDir.of("topleveldir");
        String relDir = dir.stripBranchPrefix("topleveldir/branches/mydir").get();
        assertEquals("mydir", relDir);
        assertFalse(dir.stripBranchPrefix(null).isPresent());
        assertFalse(dir.stripBranchPrefix("topleveldir/branches").isPresent());
        assertFalse(dir.stripBranchPrefix("topleveldir").isPresent());
        assertFalse(dir.stripBranchPrefix("topleveldir/otherdir/1234").isPresent());
    }
}