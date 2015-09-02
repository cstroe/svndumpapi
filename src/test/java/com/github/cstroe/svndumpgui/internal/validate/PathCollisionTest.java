package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpError;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;
import org.jmock.Mockery;
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
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void detect_invalid_dump() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/invalid/svn_add_directory_twice.invalid");
        SvnDumpValidator validator = new PathCollision();
        assertFalse(validator.validate(dump));

        SvnDumpError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision(), is(2));
        assertThat(error.getNode().get(SvnNodeHeader.PATH), is(equalTo("testdir")));
    }

    @Test
    public void validate_inner_dir_rm() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/inner_dir.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_file_deletes() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_dir_deletes() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_dir_delete.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_file_add_delete_add() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/add_edit_delete_add.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_composite_commit() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/composite_commit.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void validate_undelete() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/undelete.dump");
        SvnDumpValidator validator = new PathCollision();
        if(!validator.validate(dump)) {
            falsePositive(validator);
        }
    }

    @Test
    public void detect_invalid_copy() throws ParseException {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/invalid/undelete.invalid");
        SvnDumpValidator validator = new PathCollision();
        assertFalse("The validator should detect when copying files from nonexisting location",
            validator.validate(dump));

        SvnDumpError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision(), is(3));
        assertThat(error.getNode().get(SvnNodeHeader.PATH), is(equalTo("file2.txt")));
    }

    @Test
    public void terminate_early_on_error() {
        SvnDump dump = new SvnDumpImpl();

        SvnNode duplicateNode = new SvnNodeImpl();
        duplicateNode.getHeaders().put(SvnNodeHeader.ACTION, "add");
        duplicateNode.getHeaders().put(SvnNodeHeader.KIND, "dir");
        duplicateNode.getHeaders().put(SvnNodeHeader.PATH, "trunk");

        {
            SvnRevision r0 = new SvnRevisionImpl(0, "2015-08-30T07:23:07.042627Z");
            r0.getProperties().put(SvnProperty.AUTHOR, "testUser");
            dump.getRevisions().add(r0);
        } {
            SvnRevision r1 = new SvnRevisionImpl(1, "2015-08-30T07:24:07.042627Z");
            r1.getProperties().put(SvnProperty.AUTHOR, "testUser");
            r1.getNodes().add(duplicateNode);
            dump.getRevisions().add(r1);
        } {
            SvnRevision r2 = new SvnRevisionImpl(2, "2015-08-30T07:25:07.042627Z");
            r2.getProperties().put(SvnProperty.AUTHOR, "testUser");
            r2.getNodes().add(duplicateNode);
            dump.getRevisions().add(r2);
        } {
            Mockery mockery = new Mockery();

            SvnRevision r3 = mockery.mock(SvnRevision.class);
            dump.getRevisions().add(r3);
        }

        SvnDumpValidator pcValidator = new PathCollision();

        assertFalse(pcValidator.validate(dump));
    }
}