package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.cstroe.svndumpgui.api.SvnProperty;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnRevisionImplTest {

    @Test
    public void revision_number() {
        SvnRevisionImpl svnRevision = new SvnRevisionImpl(1);
        assertThat(svnRevision.getNumber(), is(1));
        assertNull(svnRevision.get(SvnProperty.DATE));
    }

    @Test
    public void revision_date() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(2015, Calendar.AUGUST, 26);

        SvnRevisionImpl svnRevision = new SvnRevisionImpl(1, cal.getTime().toString());
        assertThat(svnRevision.getNumber(), is(1));
        assertThat(svnRevision.get(SvnProperty.DATE), is(equalTo(cal.getTime().toString())));
    }

    @Test(expected = NullPointerException.class)
    public void set_null_properties() {
        SvnRevisionImpl  svnRevision = new SvnRevisionImpl(0);
        svnRevision.setProperties(null);
    }

    @Test(expected = NullPointerException.class)
    public void add_null_node() {
        SvnRevisionImpl  svnRevision = new SvnRevisionImpl(0);
        svnRevision.addNode(null);
    }

    @Test
    public void copy_constructor_makes_deep_copy() {
        SvnRevisionImpl revision = new SvnRevisionImpl(1);

        Map<String, String> propertiesMap = new LinkedHashMap<>();
        propertiesMap.put(SvnProperty.AUTHOR, "someAuthor");
        propertiesMap.put(SvnProperty.DATE, "someDate");
        propertiesMap.put(SvnProperty.LOG, "Log message here.");

        revision.setProperties(Collections.unmodifiableMap(propertiesMap));

        SvnNodeImpl n1 = new SvnNodeImpl(revision);
        {
            Map<SvnNodeHeader, String> headersMap = new LinkedHashMap<>();
            headersMap.put(SvnNodeHeader.ACTION, "add");
            headersMap.put(SvnNodeHeader.PATH, "aDir");
            headersMap.put(SvnNodeHeader.KIND, "dir");
            n1.setHeaders(Collections.unmodifiableMap(headersMap));
        }
        SvnNodeImpl n2 = new SvnNodeImpl(revision);
        {
            Map<SvnNodeHeader, String> headersMap = new LinkedHashMap<>();
            headersMap.put(SvnNodeHeader.ACTION, "add");
            headersMap.put(SvnNodeHeader.PATH, "anotherDir");
            headersMap.put(SvnNodeHeader.KIND, "dir");
            n2.setHeaders(Collections.unmodifiableMap(headersMap));
        }
        SvnNodeImpl n3 = new SvnNodeImpl(revision);
        {
            Map<SvnNodeHeader, String> headersMap = new LinkedHashMap<>();
            headersMap.put(SvnNodeHeader.ACTION, "add");
            headersMap.put(SvnNodeHeader.PATH, "dir3");
            headersMap.put(SvnNodeHeader.KIND, "dir");
            n3.setHeaders(Collections.unmodifiableMap(headersMap));
        }

        List<SvnNode> nodeList = new ArrayList<>();
        nodeList.add(n1);
        nodeList.add(n2);
        nodeList.add(n3);

        revision.setNodes(Collections.unmodifiableList(nodeList));

        SvnRevisionImpl revisionCopy = new SvnRevisionImpl(revision);

        nodeList.clear();

        assertThat(revisionCopy.getNumber(), is(revision.getNumber()));
        assertThat(revisionCopy.getProperties().get(SvnProperty.AUTHOR), is(equalTo("someAuthor")));
        assertThat(revisionCopy.getProperties().get(SvnProperty.DATE), is(equalTo("someDate")));
        assertThat(revisionCopy.getProperties().get(SvnProperty.LOG), is(equalTo("Log message here.")));

        assertThat(revisionCopy.getNodes().get(0).getHeaders().get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(revisionCopy.getNodes().get(0).getHeaders().get(SvnNodeHeader.PATH), is(equalTo("aDir")));
        assertThat(revisionCopy.getNodes().get(0).getHeaders().get(SvnNodeHeader.KIND), is(equalTo("dir")));

        assertThat(revisionCopy.getNodes().get(1).getHeaders().get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(revisionCopy.getNodes().get(1).getHeaders().get(SvnNodeHeader.PATH), is(equalTo("anotherDir")));
        assertThat(revisionCopy.getNodes().get(1).getHeaders().get(SvnNodeHeader.KIND), is(equalTo("dir")));

        assertThat(revisionCopy.getNodes().get(2).getHeaders().get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(revisionCopy.getNodes().get(2).getHeaders().get(SvnNodeHeader.PATH), is(equalTo("dir3")));
        assertThat(revisionCopy.getNodes().get(2).getHeaders().get(SvnNodeHeader.KIND), is(equalTo("dir")));
    }
}