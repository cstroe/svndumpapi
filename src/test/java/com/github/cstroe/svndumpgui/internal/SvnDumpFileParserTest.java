package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnProperties;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SvnDumpFileParserTest {

    @Test
    public void simple_property_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/simple_property.fragment");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);

        SvnRevisionImpl revision = new SvnRevisionImpl(0);
        parser.Property(revision);

        assertNotNull(revision.getProperty(SvnProperties.DATE));
        assertThat(revision.getProperty(SvnProperties.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void empty_revision_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/empty_revision.fragment");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);
        SvnRevision revision = parser.Revision();

        assertNotNull(revision);
        assertThat(revision.getNumber(), is(0));
        assertThat(revision.getProperty(SvnProperties.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void should_parse_empty_dump() throws IOException, ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/empty.dump");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);
        SvnDump dump = parser.Start();

        assertNotNull(dump);

        List<SvnRevision> revisionList = dump.getRevisions();

        assertThat("There should be a revision present", revisionList.size(), is(1));

        SvnRevision firstRevision = revisionList.get(0);

        assertThat(firstRevision.getNumber(), is(0));
        assertNotNull(firstRevision.getProperty(SvnProperties.DATE));
        assertThat(firstRevision.getProperty(SvnProperties.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

}
