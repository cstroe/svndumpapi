package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

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

        firstNode.addFileContentChunk(new FileContentChunkImpl(content));

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

    @Test
    public void copy_constructor_with_null_properties() {
        SvnNode node = new SvnNodeImpl();
        {
            Map<SvnNodeHeader, String> headers = new LinkedHashMap<>();
            headers.put(SvnNodeHeader.ACTION, "add");
            headers.put(SvnNodeHeader.KIND, "dir");
            headers.put(SvnNodeHeader.PATH, "dir1");
            node.setHeaders(headers);
            node.setProperties(null);
        }

        SvnNode duplicate = new SvnNodeImpl(node);

        assertThat(duplicate.getProperties().isEmpty(), is(true));
        assertFalse(duplicate.getRevision().isPresent());
        assertThat(duplicate.getHeaders().size(), is(3));
        assertThat(duplicate.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(duplicate.get(SvnNodeHeader.KIND), is(equalTo("dir")));
        assertThat(duplicate.get(SvnNodeHeader.PATH), is(equalTo("dir1")));
        assertTrue(duplicate.getContent().isEmpty());
    }

    @Test
    public void get_properties_should_return_empty_map() {
        SvnNodeImpl node = new SvnNodeImpl();
        assertNotNull(node.getProperties());
        assertTrue(node.getProperties().isEmpty());
    }

    @Test
    public void toString_works() {
        SvnNodeImpl node = new SvnNodeImpl();

        Map<SvnNodeHeader, String> headers = new HashMap<>();
        headers.put(SvnNodeHeader.MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(SvnNodeHeader.COPY_FROM_PATH, "some/old/path.txt");
        headers.put(SvnNodeHeader.COPY_FROM_REV, "2");
        headers.put(SvnNodeHeader.SOURCE_MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(SvnNodeHeader.ACTION, "add");
        headers.put(SvnNodeHeader.KIND, "file");
        headers.put(SvnNodeHeader.PATH, "new/path.txt");
        node.setHeaders(headers);

        assertThat(node.toString(), is(equalTo("add file new/path.txt eff2191c7e5abb19d79e8bcb2f1b7f38 -- copied from: some/old/path.txt@2 eff2191c7e5abb19d79e8bcb2f1b7f38")));
    }

    @Test
    public void toString_without_source_md5() {
        SvnNodeImpl node = new SvnNodeImpl();

        Map<SvnNodeHeader, String> headers = new HashMap<>();
        headers.put(SvnNodeHeader.MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(SvnNodeHeader.COPY_FROM_PATH, "some/old/path.txt");
        headers.put(SvnNodeHeader.COPY_FROM_REV, "2");
        headers.put(SvnNodeHeader.ACTION, "add");
        headers.put(SvnNodeHeader.KIND, "file");
        headers.put(SvnNodeHeader.PATH, "new/path.txt");
        node.setHeaders(headers);

        assertThat(node.toString(), is(equalTo("add file new/path.txt eff2191c7e5abb19d79e8bcb2f1b7f38 -- copied from: some/old/path.txt@2")));
    }

    @Test
    public void toString_with_size() {
        SvnNodeImpl node = new SvnNodeImpl();

        Map<SvnNodeHeader, String> headers = new HashMap<>();
        headers.put(SvnNodeHeader.MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(SvnNodeHeader.COPY_FROM_PATH, "some/old/path.txt");
        headers.put(SvnNodeHeader.COPY_FROM_REV, "2");
        headers.put(SvnNodeHeader.SOURCE_MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(SvnNodeHeader.ACTION, "add");
        headers.put(SvnNodeHeader.KIND, "file");
        headers.put(SvnNodeHeader.PATH, "new/path.txt");
        headers.put(SvnNodeHeader.TEXT_CONTENT_LENGTH, "123456");
        node.setHeaders(headers);

        assertThat(node.toString(), is(equalTo("add file new/path.txt eff2191c7e5abb19d79e8bcb2f1b7f38 -- copied from: some/old/path.txt@2 eff2191c7e5abb19d79e8bcb2f1b7f38 Size: 123456")));
    }
}