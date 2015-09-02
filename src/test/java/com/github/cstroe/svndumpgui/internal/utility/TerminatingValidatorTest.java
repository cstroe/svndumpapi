package com.github.cstroe.svndumpgui.internal.utility;

import com.github.cstroe.svndumpgui.api.SvnDumpError;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;
import com.github.cstroe.svndumpgui.api.SvnDumpValidator;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;


public class TerminatingValidatorTest {
    @Test(expected = AssertionError.class)
    public void validator_terminates() {
        Mockery context = new Mockery();

        SvnDumpValidator failingValidator = context.mock(SvnDumpValidator.class);
        SvnDumpPreamble svnDumpPreamble = context.mock(SvnDumpPreamble.class);
        SvnDumpError svnDumpError = context.mock(SvnDumpError.class);
        context.checking(new Expectations() {{
            allowing(failingValidator).isValid(); will(returnValue(false));
            allowing(failingValidator).consume(svnDumpPreamble);
            allowing(failingValidator).getError(); will(returnValue(svnDumpError));
            allowing(svnDumpError).getMessage(); will(returnValue("error message"));
        }});

        SvnDumpValidator validator = new TerminatingValidator(failingValidator);
        validator.consume(svnDumpPreamble);
    }
}