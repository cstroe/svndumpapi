package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpParser;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

public class SvnDumpParserImplTest {

    @Test
    public void should_parse_empty_dump() throws IOException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/empty.dump");

        SvnDumpParser parser = new SvnDumpParserImpl();
        SvnDump dump = parser.parse(s);

        assertNotNull(dump);

        List<SvnRevision> revisionList = dump.getRevisions();

        assertThat("There should be a revision present", revisionList.size(), is(1));

        SvnRevision firstRevision = revisionList.get(0);
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(2015, Calendar.AUGUST, 7, 13, 52, 20);

        assertThat(firstRevision.getNumber(), is(0));
        assertNotNull(firstRevision.getDate());
        assertThat(firstRevision.getDate(), is(equalTo(cal.getTime())));
    }

}
