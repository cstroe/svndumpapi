package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnDumpValidationError;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;


public class TerminatingValidatorTest {
    @Test(expected = AssertionError.class)
    public void validator_terminates() {
        Mockery context = new Mockery();

        SvnDumpValidator failingValidator = context.mock(SvnDumpValidator.class);
        SvnDumpPreamble svnDumpPreamble = context.mock(SvnDumpPreamble.class);
        SvnDumpValidationError svnDumpValidationError = context.mock(SvnDumpValidationError.class);
        context.checking(new Expectations() {{
            allowing(failingValidator).isValid(); will(returnValue(false));
            allowing(failingValidator).consume(svnDumpPreamble);
            allowing(failingValidator).getError(); will(returnValue(svnDumpValidationError));
            allowing(svnDumpValidationError).getMessage(); will(returnValue("error message"));
        }});

        SvnDumpValidator validator = new TerminatingValidator(failingValidator);
        validator.consume(svnDumpPreamble);
    }

    @Test
    public void validator_provides_error() {
        Mockery context = new Mockery();

        SvnDumpValidator failingValidator = context.mock(SvnDumpValidator.class);
        SvnDumpPreamble svnDumpPreamble = context.mock(SvnDumpPreamble.class);
        SvnDumpValidationError svnDumpValidationError = context.mock(SvnDumpValidationError.class);
        context.checking(new Expectations() {{
            allowing(failingValidator).isValid(); will(returnValue(false));
            allowing(failingValidator).consume(svnDumpPreamble);
            allowing(failingValidator).getError(); will(returnValue(svnDumpValidationError));
            allowing(svnDumpValidationError).getMessage(); will(returnValue("error message"));
        }});

        SvnDumpValidator validator = new TerminatingValidator(failingValidator);
        try {
            validator.consume(svnDumpPreamble);
            fail();
        } catch(AssertionError error) {
            assertThat(validator.isValid(), is(false));
            assertThat(validator.getError(), is(equalTo(svnDumpValidationError)));
        }
    }

    @Test
    public void valid_dump() throws ParseException {
        Mockery context = new Mockery();

        SvnDumpValidator innerValidator = context.mock(SvnDumpValidator.class, "innerValidator");

        context.checking(new Expectations() {{
            allowing(innerValidator).consume(with(any(SvnDumpPreamble.class)));
            allowing(innerValidator).consume(with(any(SvnRevision.class)));
            allowing(innerValidator).consume(with(any(SvnNode.class)));
            allowing(innerValidator).consume(with(any(FileContentChunk.class)));
            allowing(innerValidator).endChunks();
            allowing(innerValidator).endNode(with(any(SvnNode.class)));
            allowing(innerValidator).endRevision(with(any(SvnRevision.class)));
            allowing(innerValidator).finish();

            allowing(innerValidator).isValid(); will(returnValue(true));
            allowing(innerValidator).getError(); will(returnValue(null));
        }});

        TerminatingValidator terminatingValidator = new TerminatingValidator(innerValidator);

        SvnDumpFileParser.consume(TestUtil.openResource("dumps/svn_delete_with_add.dump"), terminatingValidator);
    }
}