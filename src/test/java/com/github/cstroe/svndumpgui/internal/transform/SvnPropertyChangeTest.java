package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.internal.SvnNodeImpl;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnPropertyChangeTest {

    @Test
    public void property_should_be_changed() {
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
    public void property_should_be_deleted() {
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

}