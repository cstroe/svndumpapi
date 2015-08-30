package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpError;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PathCollisionTest {

    private void falsePositive(SvnDumpValidator validator) {
        throw new AssertionError("False positive. This is what the validator says, but it's not correct:\n\n" + validator.getError().getMessage() + "\n\n");
    }

    @Test
    public void detect_valid_dump() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_copy_file.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.isValid(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void detect_invalid_dump() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/invalid/svn_add_directory_twice.invalid");
        SvnDumpValidator validator = new PathCollision();
        assertFalse(validator.isValid(dump));

        SvnDumpError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision().getNumber(), is(2));
        assertThat(error.getNode().get(SvnNodeHeader.PATH), is(equalTo("testdir")));
    }

    @Test
    public void validate_inner_dir_rm() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/inner_dir.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.isValid(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_file_deletes() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.isValid(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_dir_deletes() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_dir_delete.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.isValid(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_file_add_delete_add() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/add_edit_delete_add.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.isValid(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_composite_commit() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/composite_commit.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.isValid(dump)) {
            falsePositive(validator);
        }
    }
}