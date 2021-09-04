package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryValidator;
import com.github.cstroe.svndumpgui.api.RepositoryValidationError;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.RepositoryImpl;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpParserDoppelganger;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class PathCollisionValidatorTest {

    private void falseNegative(RepositoryValidator validator) {
        throw new AssertionError("False negative. This is what the validator says, but it's not correct:\n\n" + validator.getError().getMessage() + "\n\n");
    }

    @Test
    public void detect_valid_dump() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/svn_copy_file.dump", validator);

        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void detect_invalid_dump() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/invalid/svn_add_directory_twice.invalid", validator);

        assertFalse("The validator should detect the invalid condition of adding the same directory twice.", validator.isValid());

        RepositoryValidationError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision(), is(2));
        assertThat(error.getNode().get(NodeHeader.PATH), is(equalTo("testdir")));
    }

    @Test
    public void validate_inner_dir_rm() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/inner_dir.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_file_deletes() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/svn_multi_file_delete.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_dir_deletes() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/svn_multi_dir_delete.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_file_add_delete_add() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/add_edit_delete_add.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_composite_commit() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/composite_commit.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void validate_undelete() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/undelete.dump", validator);
        if(!validator.isValid()) {
            falseNegative(validator);
        }
    }

    @Test
    public void detect_invalid_copy() throws ParseException {
        final RepositoryValidator validator = new PathCollisionValidator();
        SvnDumpFileParserTest.consume("dumps/invalid/undelete.invalid", validator);

        assertFalse("The validator should detect the invalid condition of copying files from nonexisting location",
            validator.isValid());

        RepositoryValidationError error = validator.getError();
        assertNotNull(error.getMessage());
        assertThat(error.getRevision(), is(3));
        assertThat(error.getNode().get(NodeHeader.PATH), is(equalTo("file2.txt")));
    }

    @Test
    public void terminate_early_on_error() {
        Repository dump = new RepositoryImpl();

        {
            Revision r0 = new RevisionImpl(0, "2015-08-30T07:23:07.042627Z");
            r0.getProperties().put(Property.AUTHOR, "testUser");
            dump.getRevisions().add(r0);
        } {
            Revision r1 = new RevisionImpl(1, "2015-08-30T07:24:07.042627Z");
            r1.getProperties().put(Property.AUTHOR, "testUser");
            r1.getNodes().add(createErrorTriggeringNode(r1));
            dump.getRevisions().add(r1);
        } {
            Revision r2 = new RevisionImpl(2, "2015-08-30T07:25:07.042627Z");
            r2.getProperties().put(Property.AUTHOR, "testUser");
            r2.getNodes().add(createErrorTriggeringNode(r2));
            dump.getRevisions().add(r2);
        } {
            Mockery mockery = new Mockery();

            Revision r3 = mockery.mock(Revision.class, "r3");
            dump.getRevisions().add(r3);

            final List<Node> nodeList = new ArrayList<>();

            Node n3_1 = mockery.mock(Node.class, "n3_1");
            nodeList.add(n3_1);

            final List<ContentChunk> emptyList = new LinkedList<>();

            mockery.checking(new Expectations() {{
                oneOf(r3).getNodes(); will(returnValue(nodeList));
                oneOf(n3_1).getContent(); will(returnValue(emptyList));
            }});
        }

        RepositoryValidator pcValidator = new PathCollisionValidator();
        SvnDumpParserDoppelganger.consumeWithoutChaining(dump, pcValidator);
        assertFalse(pcValidator.isValid());
    }

    private Node createErrorTriggeringNode(Revision r) {
        Node duplicateNode = new NodeImpl(r);
        duplicateNode.getHeaders().put(NodeHeader.ACTION, "add");
        duplicateNode.getHeaders().put(NodeHeader.KIND, "dir");
        duplicateNode.getHeaders().put(NodeHeader.PATH, "trunk");
        return duplicateNode;
    }
}