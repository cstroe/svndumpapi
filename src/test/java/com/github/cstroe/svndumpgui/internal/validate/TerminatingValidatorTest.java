package com.github.cstroe.svndumpgui.internal.validate;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.RepositoryValidationError;
import com.github.cstroe.svndumpgui.api.RepositoryValidator;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
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

        RepositoryValidator failingValidator = context.mock(RepositoryValidator.class);
        Preamble preamble = context.mock(Preamble.class);
        RepositoryValidationError repositoryValidationError = context.mock(RepositoryValidationError.class);
        context.checking(new Expectations() {{
            allowing(failingValidator).isValid(); will(returnValue(false));
            allowing(failingValidator).consume(preamble);
            allowing(failingValidator).getError(); will(returnValue(repositoryValidationError));
            allowing(repositoryValidationError).getMessage(); will(returnValue("error message"));
        }});

        RepositoryValidator validator = new TerminatingValidator(failingValidator);
        validator.consume(preamble);
    }

    @Test
    public void validator_provides_error() {
        Mockery context = new Mockery();

        RepositoryValidator failingValidator = context.mock(RepositoryValidator.class);
        Preamble preamble = context.mock(Preamble.class);
        RepositoryValidationError repositoryValidationError = context.mock(RepositoryValidationError.class);
        context.checking(new Expectations() {{
            allowing(failingValidator).isValid(); will(returnValue(false));
            allowing(failingValidator).consume(preamble);
            allowing(failingValidator).getError(); will(returnValue(repositoryValidationError));
            allowing(repositoryValidationError).getMessage(); will(returnValue("error message"));
        }});

        RepositoryValidator validator = new TerminatingValidator(failingValidator);
        try {
            validator.consume(preamble);
            fail();
        } catch(AssertionError error) {
            assertThat(validator.isValid(), is(false));
            assertThat(validator.getError(), is(equalTo(repositoryValidationError)));
        }
    }

    @Test
    public void valid_dump() throws ParseException {
        Mockery context = new Mockery();

        RepositoryValidator innerValidator = context.mock(RepositoryValidator.class, "innerValidator");

        context.checking(new Expectations() {{
            allowing(innerValidator).consume(with(any(Preamble.class)));
            allowing(innerValidator).consume(with(any(Revision.class)));
            allowing(innerValidator).consume(with(any(Node.class)));
            allowing(innerValidator).consume(with(any(ContentChunk.class)));
            allowing(innerValidator).endChunks();
            allowing(innerValidator).endNode(with(any(Node.class)));
            allowing(innerValidator).endRevision(with(any(Revision.class)));
            allowing(innerValidator).finish();

            allowing(innerValidator).isValid(); will(returnValue(true));
            allowing(innerValidator).getError(); will(returnValue(null));
        }});

        TerminatingValidator terminatingValidator = new TerminatingValidator(innerValidator);

        SvnDumpParser.consume(TestUtil.openResource("dumps/svn_delete_with_add.dump"), terminatingValidator);
    }
}