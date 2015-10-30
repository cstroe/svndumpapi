package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Property;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnRevisionImplTest {

    @Test
    public void revision_number() {
        {
            RevisionImpl svnRevision = new RevisionImpl(1);
            assertThat(svnRevision.getNumber(), is(1));
            assertNull(svnRevision.get(Property.DATE));
        }{
            RevisionImpl svnRevision = new RevisionImpl(1);
            svnRevision.setNumber(2);
            assertThat(svnRevision.getNumber(), is(2));
            assertNull(svnRevision.get(Property.DATE));
        }
    }

    @Test
    public void revision_date() {
        Calendar cal = new GregorianCalendar();
        cal.clear();
        cal.set(2015, Calendar.AUGUST, 26);

        RevisionImpl svnRevision = new RevisionImpl(1, cal.getTime().toString());
        assertThat(svnRevision.getNumber(), is(1));
        assertThat(svnRevision.get(Property.DATE), is(equalTo(cal.getTime().toString())));
    }

    @Test(expected = NullPointerException.class)
    public void set_null_properties() {
        RevisionImpl svnRevision = new RevisionImpl(0);
        svnRevision.setProperties(null);
    }

    @Test(expected = NullPointerException.class)
    public void add_null_node() {
        RevisionImpl svnRevision = new RevisionImpl(0);
        svnRevision.addNode(null);
    }

    @Test
    public void copy_constructor_makes_deep_copy() {
        RevisionImpl revision = new RevisionImpl(1);

        Map<String, String> propertiesMap = new LinkedHashMap<>();
        propertiesMap.put(Property.AUTHOR, "someAuthor");
        propertiesMap.put(Property.DATE, "someDate");
        propertiesMap.put(Property.LOG, "Log message here.");

        revision.setProperties(Collections.unmodifiableMap(propertiesMap));

        NodeImpl n1 = new NodeImpl(revision);
        {
            Map<NodeHeader, String> headersMap = new LinkedHashMap<>();
            headersMap.put(NodeHeader.ACTION, "add");
            headersMap.put(NodeHeader.PATH, "aDir");
            headersMap.put(NodeHeader.KIND, "dir");
            n1.setHeaders(Collections.unmodifiableMap(headersMap));
        }
        NodeImpl n2 = new NodeImpl(revision);
        {
            Map<NodeHeader, String> headersMap = new LinkedHashMap<>();
            headersMap.put(NodeHeader.ACTION, "add");
            headersMap.put(NodeHeader.PATH, "anotherDir");
            headersMap.put(NodeHeader.KIND, "dir");
            n2.setHeaders(Collections.unmodifiableMap(headersMap));
        }
        NodeImpl n3 = new NodeImpl(revision);
        {
            Map<NodeHeader, String> headersMap = new LinkedHashMap<>();
            headersMap.put(NodeHeader.ACTION, "add");
            headersMap.put(NodeHeader.PATH, "dir3");
            headersMap.put(NodeHeader.KIND, "dir");
            n3.setHeaders(Collections.unmodifiableMap(headersMap));
        }

        List<Node> nodeList = new ArrayList<>();
        nodeList.add(n1);
        nodeList.add(n2);
        nodeList.add(n3);

        revision.setNodes(Collections.unmodifiableList(nodeList));

        RevisionImpl revisionCopy = new RevisionImpl(revision);

        nodeList.clear();

        assertThat(revisionCopy.getNumber(), is(revision.getNumber()));
        assertThat(revisionCopy.getProperties().get(Property.AUTHOR), is(equalTo("someAuthor")));
        assertThat(revisionCopy.getProperties().get(Property.DATE), is(equalTo("someDate")));
        assertThat(revisionCopy.getProperties().get(Property.LOG), is(equalTo("Log message here.")));

        assertThat(revisionCopy.getNodes().get(0).getHeaders().get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(revisionCopy.getNodes().get(0).getHeaders().get(NodeHeader.PATH), is(equalTo("aDir")));
        assertThat(revisionCopy.getNodes().get(0).getHeaders().get(NodeHeader.KIND), is(equalTo("dir")));

        assertThat(revisionCopy.getNodes().get(1).getHeaders().get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(revisionCopy.getNodes().get(1).getHeaders().get(NodeHeader.PATH), is(equalTo("anotherDir")));
        assertThat(revisionCopy.getNodes().get(1).getHeaders().get(NodeHeader.KIND), is(equalTo("dir")));

        assertThat(revisionCopy.getNodes().get(2).getHeaders().get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(revisionCopy.getNodes().get(2).getHeaders().get(NodeHeader.PATH), is(equalTo("dir3")));
        assertThat(revisionCopy.getNodes().get(2).getHeaders().get(NodeHeader.KIND), is(equalTo("dir")));
    }

    @Test
    public void descriptive_toString() {
        {
            RevisionImpl revision = new RevisionImpl(2);
            assertThat(revision.toString(), is(equalTo("Revision: 2, *** empty message ***")));
        } {
            RevisionImpl revision = new RevisionImpl(2, "someDate");
            assertThat(revision.toString(), is(equalTo("Revision: 2, *** empty message *** - someDate")));
        } {
            RevisionImpl revision = new RevisionImpl(2, "someDate");
            revision.getProperties().put(Property.LOG, "A log message.");
            assertThat(revision.toString(), is(equalTo("Revision: 2, A log message. - someDate")));
        } {
            RevisionImpl revision = new RevisionImpl(2, "someDate");
            revision.getProperties().put(Property.LOG, "A log message.");
            revision.getProperties().put(Property.AUTHOR, "developer1");
            assertThat(revision.toString(), is(equalTo("Revision: 2, A log message. - developer1 @ someDate")));
        } {
            RevisionImpl revision = new RevisionImpl(2, "someDate");
            revision.getProperties().put(Property.AUTHOR, "developer1");
            assertThat(revision.toString(), is(equalTo("Revision: 2, *** empty message *** - developer1 @ someDate")));
        }
    }
}