package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class SvnDumpParserImplTest {

    @Test
    public void should_parse_empty_dump() throws IOException, ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/empty.dump");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);
        parser.Start(System.out);

//        assertNotNull(dump);

//        List<SvnRevision> revisionList = dump.getRevisions();

//        assertThat("There should be a revision present", revisionList.size(), is(1));

//        SvnRevision firstRevision = revisionList.get(0);
//        Calendar cal = new GregorianCalendar();
//        cal.clear();
//        cal.set(2015, Calendar.AUGUST, 7, 13, 52, 20);

//        assertThat(firstRevision.getNumber(), is(0));
//        assertNotNull(firstRevision.getDate());
//        assertThat(firstRevision.getDate(), is(equalTo(cal.getTime())));
    }

}
