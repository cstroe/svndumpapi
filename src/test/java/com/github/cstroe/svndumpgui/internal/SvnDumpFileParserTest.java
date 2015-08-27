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
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SvnDumpFileParserTest {

    @SuppressWarnings("unchecked")
    @Test
    public void simple_property_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/simple_property.fragment");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);

        SvnRevisionImpl revision = new SvnRevisionImpl(0);
        Map properties = parser.Property();
        revision.setProperties(properties);

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

    @Test
    public void should_parse_one_commit() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/firstcommit.dump");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);
        SvnDump dump = parser.Start();

        assertThat("The repository dump contains two revisions.", dump.getRevisions().size(), is(2));

        assertThat(dump.getRevisions().get(0).getNumber(), is(0));
        assertThat(dump.getRevisions().get(0).getProperty(SvnProperties.DATE), is(equalTo("2015-08-27T02:50:19.465543Z")));

        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).getProperty(SvnProperties.DATE), is(equalTo("2015-08-27T05:38:16.553074Z")));
        assertThat(dump.getRevisions().get(1).getProperty(SvnProperties.AUTHOR), is(equalTo("cosmin")));
        assertThat(dump.getRevisions().get(1).getProperty(SvnProperties.LOG), is(equalTo("Added a first file.")));

        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).getPath(), is(equalTo("firstFile.txt")));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).getKind(), is(equalTo("file")));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).getAction(), is(equalTo("add")));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).getMd5(), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).getSha1(), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
    }

}
