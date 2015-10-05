package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpValidationError;
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
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PathCollisionTest {

    private void falseNegative(SvnDumpValidator validator) {
        throw new AssertionError("False negative. This is what the validator says, but it's not correct:\n\n" + validator.getError().getMessage() + "\n\n");
    }

    @Test
    public void detect_valid_dump() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/svn_copy_file.dump", validator);

        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void detect_invalid_dump() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/invalid/svn_add_directory_twice.invalid", validator);

        assertFalse("The validator should detect the invalid condition of adding the same directory twice.", validator.isValid());

        SvnDumpValidationError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision(), is(2));
        assertThat(error.getNode().get(SvnNodeHeader.PATH), is(equalTo("testdir")));
    }

    @Test
    public void validate_inner_dir_rm() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/inner_dir.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_file_deletes() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_dir_deletes() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/svn_multi_dir_delete.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_file_add_delete_add() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/add_edit_delete_add.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_composite_commit() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/composite_commit.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_undelete() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/undelete.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void detect_invalid_copy() throws ParseException {
        final SvnDumpValidator validator = new PathCollision();
        SvnDumpFileParserTest.consume("dumps/invalid/undelete.invalid", validator);

        assertFalse("The validator should detect the invalid condition of copying files from nonexisting location",
            validator.isValid());

        SvnDumpValidationError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision(), is(3));
        assertThat(error.getNode().get(SvnNodeHeader.PATH), is(equalTo("file2.txt")));
    }

    @Test
    public void terminate_early_on_error() {
        SvnDump dump = new SvnDumpImpl();

        {
            SvnRevision r0 = new SvnRevisionImpl(0, "2015-08-30T07:23:07.042627Z");
            r0.getProperties().put(SvnProperty.AUTHOR, "testUser");
            dump.getRevisions().add(r0);
        } {
            SvnRevision r1 = new SvnRevisionImpl(1, "2015-08-30T07:24:07.042627Z");
            r1.getProperties().put(SvnProperty.AUTHOR, "testUser");
            r1.getNodes().add(createErrorTriggerringNode(r1));
            dump.getRevisions().add(r1);
        } {
            SvnRevision r2 = new SvnRevisionImpl(2, "2015-08-30T07:25:07.042627Z");
            r2.getProperties().put(SvnProperty.AUTHOR, "testUser");
            r2.getNodes().add(createErrorTriggerringNode(r2));
            dump.getRevisions().add(r2);
        } {
            Mockery mockery = new Mockery();

            SvnRevision r3 = mockery.mock(SvnRevision.class, "r3");
            dump.getRevisions().add(r3);

            final List<SvnNode> nodeList = new ArrayList<>();

            SvnNode n3_1 = mockery.mock(SvnNode.class, "n3_1");
            nodeList.add(n3_1);

            final List<FileContentChunk> emptyList = new LinkedList<>();

            mockery.checking(new Expectations() {{
                oneOf(r3).getNodes(); will(returnValue(nodeList));
                oneOf(n3_1).getContent(); will(returnValue(emptyList));
            }});
        }

        SvnDumpValidator pcValidator = new PathCollision();
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(dump, pcValidator);
        assertFalse(pcValidator.isValid());
    }

    private SvnNode createErrorTriggerringNode(SvnRevision r) {
        SvnNode duplicateNode = new SvnNodeImpl(r);
        duplicateNode.getHeaders().put(SvnNodeHeader.ACTION, "add");
        duplicateNode.getHeaders().put(SvnNodeHeader.KIND, "dir");
        duplicateNode.getHeaders().put(SvnNodeHeader.PATH, "trunk");
        return duplicateNode;
    }
}