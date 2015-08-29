package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import static org.junit.Assert.*;

public class PathCollisionTest {

    @Test
    public void detect_valid_dump() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_copy_file.dump");
        SvnDumpValidator validator = new PathCollision();
        assertTrue(validator.isValid(dump));
    }

    @Test
    public void detect_invalid_dump() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/invalid/svn_add_directory_twice.invalid");
        SvnDumpValidator validator = new PathCollision();
        assertFalse(validator.isValid(dump));
    }

}