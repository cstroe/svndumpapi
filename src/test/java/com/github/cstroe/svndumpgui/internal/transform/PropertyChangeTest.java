package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import com.github.cstroe.svndumpgui.internal.transform.property.MergeInfoReplaceRevision;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;

public class PropertyChangeTest {

    @Test
    public void node_property_should_be_changed() {
        NodeImpl node = new NodeImpl();
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        node.setProperties(properties);

        PropertyChange propertyChange = new PropertyChange("prop2"::equals, t -> "d");

        propertyChange.consume(node);

        Map<String, String> newProperties = node.getProperties();
        assertThat(newProperties.size(), is(3));
        Iterator<Map.Entry<String,String>> propIter = newProperties.entrySet().iterator();

        Map.Entry<String, String> currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop1")));
        assertThat(currentEntry.getValue(), is(equalTo("val1")));

        currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop2")));
        assertThat(currentEntry.getValue(), is(equalTo("d")));

        currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop3")));
        assertThat(currentEntry.getValue(), is(equalTo("val3")));

        assertFalse(propIter.hasNext());
    }

    @Test
    public void node_property_should_be_deleted() {
        NodeImpl node = new NodeImpl();
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        node.setProperties(properties);

        PropertyChange propertyChange = new PropertyChange("prop2"::equals, t -> null);

        propertyChange.consume(node);

        Map<String, String> newProperties = node.getProperties();
        assertThat(newProperties.size(), is(2));
        Iterator<Map.Entry<String,String>> propIter = newProperties.entrySet().iterator();

        Map.Entry<String, String> currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop1")));
        assertThat(currentEntry.getValue(), is(equalTo("val1")));

        currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop3")));
        assertThat(currentEntry.getValue(), is(equalTo("val3")));

        assertFalse(propIter.hasNext());
    }

    @Test
    public void node_property_should_be_changed_and_dump_should_be_unchanged() throws ParseException {
        RepositoryConsumer propChange =
            new PropertyChange("svn:mergeinfo"::equals, new MergeInfoReplaceRevision("/branches/mybranch", 2, 1));

        RepositoryInMemory inMemoryDump = new RepositoryInMemory();
        propChange.continueTo(inMemoryDump);

        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        SvnDumpParser.consume(s, propChange);

        Repository dump = inMemoryDump.getRepo();

        assertThat(dump.getRevisions().size(), is(6));
        Revision r4 = dump.getRevisions().get(4);
        assertThat(r4.getNodes().size(), is(2));
        assertThat(r4.getNodes().get(0).getProperties().get(Property.MERGEINFO), is("/branches/mybranch:1-3"));
    }

    @Test
    public void revision_property_should_be_changed() {
        RevisionImpl revision = new RevisionImpl(2);
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        revision.setProperties(properties);

        PropertyChange propertyChange = new PropertyChange("prop2"::equals, t -> "d");

        propertyChange.consume(revision);

        Map<String, String> newProperties = revision.getProperties();
        assertThat(newProperties.size(), is(3));
        Iterator<Map.Entry<String,String>> propIter = newProperties.entrySet().iterator();

        Map.Entry<String, String> currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop1")));
        assertThat(currentEntry.getValue(), is(equalTo("val1")));

        currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop2")));
        assertThat(currentEntry.getValue(), is(equalTo("d")));

        currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop3")));
        assertThat(currentEntry.getValue(), is(equalTo("val3")));

        assertFalse(propIter.hasNext());
    }

    @Test
    public void revision_property_should_be_deleted() {
        RevisionImpl revision = new RevisionImpl(2);
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        revision.setProperties(properties);

        PropertyChange propertyChange = new PropertyChange("prop2"::equals, t -> null);

        propertyChange.consume(revision);

        Map<String, String> newProperties = revision.getProperties();
        assertThat(newProperties.size(), is(2));
        Iterator<Map.Entry<String,String>> propIter = newProperties.entrySet().iterator();

        Map.Entry<String, String> currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop1")));
        assertThat(currentEntry.getValue(), is(equalTo("val1")));

        currentEntry = propIter.next();
        assertThat(currentEntry.getKey(), is(equalTo("prop3")));
        assertThat(currentEntry.getValue(), is(equalTo("val3")));

        assertFalse(propIter.hasNext());
    }
}