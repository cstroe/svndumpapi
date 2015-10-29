package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnProperty;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;
import com.github.cstroe.svndumpgui.internal.transform.property.MergeInfoReplaceRevision;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpInMemory;
import org.junit.Test;

import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnPropertyChangeTest {

    @Test
    public void node_property_should_be_changed() {
        SvnNodeImpl node = new SvnNodeImpl();
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        node.setProperties(properties);

        SvnPropertyChange propertyChange = new SvnPropertyChange("prop2"::equals, t -> "d");

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
        SvnNodeImpl node = new SvnNodeImpl();
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        node.setProperties(properties);

        SvnPropertyChange propertyChange = new SvnPropertyChange("prop2"::equals, t -> null);

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
        SvnDumpConsumer propChange =
            new SvnPropertyChange("svn:mergeinfo"::equals, new MergeInfoReplaceRevision("/branches/mybranch", 2, 1));

        SvnDumpInMemory inMemoryDump = new SvnDumpInMemory();
        propChange.continueTo(inMemoryDump);

        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        SvnDumpFileParser.consume(s, propChange);

        SvnDump dump = inMemoryDump.getDump();

        assertThat(dump.getRevisions().size(), is(6));
        SvnRevision r4 = dump.getRevisions().get(4);
        assertThat(r4.getNodes().size(), is(2));
        assertThat(r4.getNodes().get(0).getProperties().get(SvnProperty.MERGEINFO), is("/branches/mybranch:1-3\n"));
    }

    @Test
    public void revision_property_should_be_changed() {
        SvnRevisionImpl revision = new SvnRevisionImpl(2);
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        revision.setProperties(properties);

        SvnPropertyChange propertyChange = new SvnPropertyChange("prop2"::equals, t -> "d");

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
        SvnRevisionImpl revision = new SvnRevisionImpl(2);
        Map<String, String> properties = new LinkedHashMap<>();
        properties.put("prop1", "val1");
        properties.put("prop2", "val2");
        properties.put("prop3", "val3");
        revision.setProperties(properties);

        SvnPropertyChange propertyChange = new SvnPropertyChange("prop2"::equals, t -> null);

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