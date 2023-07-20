package com.github.cstroe.svndumpgui.internal.writer.git;

import org.javatuples.Pair;
import org.javatuples.Triplet;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class PathParserTest {
    @Test
    public void parseBranchCreate() {
        PathParser parser = new PathParser();
        Optional<Triplet<ChangeType, String, String>> path = parser.parse("branches/branch");
        assertTrue(path.isPresent());
        assertEquals(ChangeType.BRANCH_CREATE, path.get().getValue0());
        assertEquals("branch", path.get().getValue1());
        assertNull(path.get().getValue2());
    }
    @Test
    public void parseBranchPath() {
        PathParser parser = new PathParser();
        Optional<Triplet<ChangeType, String, String>> path = parser.parse("branches/branch/path/file.txt");
        assertTrue(path.isPresent());
        assertEquals(ChangeType.BRANCH, path.get().getValue0());
        assertEquals("branch", path.get().getValue1());
        assertEquals("path/file.txt", path.get().getValue2());
    }
    @Test
    public void parseTagCreate() {
        PathParser parser = new PathParser();
        Optional<Triplet<ChangeType, String, String>> path = parser.parse("tags/tag");
        assertTrue(path.isPresent());
        assertEquals(ChangeType.TAG_CREATE, path.get().getValue0());
        assertEquals("tag", path.get().getValue1());
        assertNull(path.get().getValue2());
    }
    @Test
    public void parseTagPath() {
        PathParser parser = new PathParser();
        Optional<Triplet<ChangeType, String, String>> path = parser.parse("tags/tag/path/file.txt");
        assertTrue(path.isPresent());
        assertEquals(ChangeType.TAG, path.get().getValue0());
        assertEquals("tag", path.get().getValue1());
        assertEquals("path/file.txt", path.get().getValue2());
    }
}