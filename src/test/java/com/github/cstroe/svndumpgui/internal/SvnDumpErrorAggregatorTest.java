package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDumpError;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SvnDumpErrorAggregatorTest {
    @Test
    public void get_revision_returns_revision_number() {
        Mockery context = new Mockery();

        SvnDumpError e1 = context.mock(SvnDumpError.class, "e1");
        SvnDumpError e2 = context.mock(SvnDumpError.class, "e2");
        SvnDumpError e3 = context.mock(SvnDumpError.class, "e3");
        SvnDumpError e4 = context.mock(SvnDumpError.class, "e4");

        context.checking(new Expectations() {{
            allowing(e1).getRevision(); will(returnValue(2));
            allowing(e2).getRevision(); will(returnValue(3));
            allowing(e3).getRevision(); will(returnValue(1));
            allowing(e4).getRevision(); will(returnValue(4));
        }});

        SvnDumpErrorAggregator aggregator = new SvnDumpErrorAggregator();
        aggregator.add(e1);
        aggregator.add(e2);
        aggregator.add(e3);
        aggregator.add(e4);

        assertThat(aggregator.getRevision(), is(1));
    }

    @Test
    public void get_message_concatenates_messages() {
        Mockery context = new Mockery();

        SvnDumpError e1 = context.mock(SvnDumpError.class, "e1");
        SvnDumpError e2 = context.mock(SvnDumpError.class, "e2");
        SvnDumpError e3 = context.mock(SvnDumpError.class, "e3");
        SvnDumpError e4 = context.mock(SvnDumpError.class, "e4");

        context.checking(new Expectations() {{
            allowing(e1).getMessage(); will(returnValue("message1"));
            allowing(e2).getMessage(); will(returnValue("message2"));
            allowing(e3).getMessage(); will(returnValue("message3"));
            allowing(e4).getMessage(); will(returnValue("message4"));
        }});

        SvnDumpErrorAggregator aggregator = new SvnDumpErrorAggregator();
        aggregator.add(e1);
        aggregator.add(e2);
        aggregator.add(e3);
        aggregator.add(e4);

        assertThat(aggregator.getMessage(), is(equalTo("message1\n\nmessage2\n\nmessage3\n\nmessage4")));
    }
}