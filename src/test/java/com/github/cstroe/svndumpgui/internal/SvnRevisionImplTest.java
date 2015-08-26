package com.github.cstroe.svndumpgui.internal;

import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnRevisionImplTest {

    @Test
    public void revision_number() {
        SvnRevisionImpl svnRevision = new SvnRevisionImpl(1);
        assertThat(svnRevision.getNumber(), is(1));
        assertNull(svnRevision.getDate());
    }

    @Test
    public void revision_date() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(2015, Calendar.AUGUST, 26);

        SvnRevisionImpl svnRevision = new SvnRevisionImpl(1, cal.getTime());
        assertThat(svnRevision.getNumber(), is(1));
        assertThat(svnRevision.getDate(), is(equalTo(cal.getTime())));
    }

}