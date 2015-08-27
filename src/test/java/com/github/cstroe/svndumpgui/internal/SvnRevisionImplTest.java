package com.github.cstroe.svndumpgui.internal;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;
import com.github.cstroe.svndumpgui.api.SvnProperties;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnRevisionImplTest {

    @Test
    public void revision_number() {
        SvnRevisionImpl svnRevision = new SvnRevisionImpl(1);
        assertThat(svnRevision.getNumber(), is(1));
        assertNull(svnRevision.getProperty(SvnProperties.DATE));
    }

    @Test
    public void revision_date() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(2015, Calendar.AUGUST, 26);

        SvnRevisionImpl svnRevision = new SvnRevisionImpl(1, cal.getTime().toString());
        assertThat(svnRevision.getNumber(), is(1));
        assertThat(svnRevision.getProperty(SvnProperties.DATE), is(equalTo(cal.getTime().toString())));
    }

}