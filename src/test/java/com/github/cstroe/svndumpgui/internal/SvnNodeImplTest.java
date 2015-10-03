package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import org.junit.Test;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SvnNodeImplTest {

    @Test
    public void copy_constructor_makes_deep_copy() {
        SvnRevisionImpl revision = new SvnRevisionImpl(1);
        SvnNodeImpl firstNode = new SvnNodeImpl(revision);

        Map<SvnNodeHeader, String> headersMap = new LinkedHashMap<>();
        headersMap.put(SvnNodeHeader.ACTION, "add");
        headersMap.put(SvnNodeHeader.PATH, "someDir");
        headersMap.put(SvnNodeHeader.KIND, "dir");
        firstNode.setHeaders(Collections.unmodifiableMap(headersMap));

        Map<String, String> propertiesMap = new LinkedHashMap<>();
        propertiesMap.put("prop1", "valX");
        propertiesMap.put("prop3", "valZ");
        propertiesMap.put("prop2", "valY");
        firstNode.setProperties(Collections.unmodifiableMap(propertiesMap));

        byte[] content = new byte[5];
        content[0] = 'a';
        content[1] = 'b';
        content[2] = 'c';
        content[3] = 'd';
        content[4] = 'e';

        firstNode.addFileContentChunk(new FileContentChunk(content));

        SvnNodeImpl secondNode = new SvnNodeImpl(firstNode);

        content[0] = 'X';
        content[1] = 'X';
        content[2] = 'X';
        content[3] = 'X';
        content[4] = 'X';
        propertiesMap.clear();
        headersMap.clear();

        assertThat(secondNode.getContent().size(), is(1));
        assertThat(new String(secondNode.getContent().get(0).getContent()), is(equalTo("abcde")));

        {
            assertThat(secondNode.getHeaders().size(), is(3));
            Iterator<Map.Entry<SvnNodeHeader, String>> headerIterator = secondNode.getHeaders().entrySet().iterator();

            Map.Entry<SvnNodeHeader, String> currentHeader = headerIterator.next();
            assertThat(currentHeader.getKey(), is(SvnNodeHeader.ACTION));
            assertThat(currentHeader.getValue(), is(equalTo("add")));

            currentHeader = headerIterator.next();
            assertThat(currentHeader.getKey(), is(SvnNodeHeader.PATH));
            assertThat(currentHeader.getValue(), is(equalTo("someDir")));

            currentHeader = headerIterator.next();
            assertThat(currentHeader.getKey(), is(SvnNodeHeader.KIND));
            assertThat(currentHeader.getValue(), is(equalTo("dir")));
        }
        {
            assertThat(secondNode.getProperties().size(), is(3));
            Iterator<Map.Entry<String, String>> propertiesIterator = secondNode.getProperties().entrySet().iterator();

            Map.Entry<String, String> currentProperty = propertiesIterator.next();
            assertThat(currentProperty.getKey(), is(equalTo("prop1")));
            assertThat(currentProperty.getValue(), is(equalTo("valX")));

            currentProperty = propertiesIterator.next();
            assertThat(currentProperty.getKey(), is(equalTo("prop3")));
            assertThat(currentProperty.getValue(), is(equalTo("valZ")));

            currentProperty = propertiesIterator.next();
            assertThat(currentProperty.getKey(), is(equalTo("prop2")));
            assertThat(currentProperty.getValue(), is(equalTo("valY")));
        }
    }

    @Test
    public void copy_constructor_works_for_empty_node() {
        SvnRevisionImpl revision = new SvnRevisionImpl(1);
        SvnNodeImpl node = new SvnNodeImpl(revision);
        new SvnNodeImpl(node);
    }
}