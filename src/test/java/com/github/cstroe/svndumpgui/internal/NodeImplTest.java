package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
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

public class NodeImplTest {

    @Test
    public void copy_constructor_makes_deep_copy() {
        RevisionImpl revision = new RevisionImpl(1);
        NodeImpl firstNode = new NodeImpl(revision);

        Map<NodeHeader, String> headersMap = new LinkedHashMap<>();
        headersMap.put(NodeHeader.ACTION, "add");
        headersMap.put(NodeHeader.PATH, "someDir");
        headersMap.put(NodeHeader.KIND, "dir");
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

        firstNode.addFileContentChunk(new ContentChunkImpl(content));

        NodeImpl secondNode = new NodeImpl(firstNode);

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
            Iterator<Map.Entry<NodeHeader, String>> headerIterator = secondNode.getHeaders().entrySet().iterator();

            Map.Entry<NodeHeader, String> currentHeader = headerIterator.next();
            assertThat(currentHeader.getKey(), is(NodeHeader.ACTION));
            assertThat(currentHeader.getValue(), is(equalTo("add")));

            currentHeader = headerIterator.next();
            assertThat(currentHeader.getKey(), is(NodeHeader.PATH));
            assertThat(currentHeader.getValue(), is(equalTo("someDir")));

            currentHeader = headerIterator.next();
            assertThat(currentHeader.getKey(), is(NodeHeader.KIND));
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
        RevisionImpl revision = new RevisionImpl(1);
        NodeImpl node = new NodeImpl(revision);
        new NodeImpl(node);
    }

    @Test
    public void copy_constructor_with_null_properties() {
        Node node = new NodeImpl();
        {
            Map<NodeHeader, String> headers = new LinkedHashMap<>();
            headers.put(NodeHeader.ACTION, "add");
            headers.put(NodeHeader.KIND, "dir");
            headers.put(NodeHeader.PATH, "dir1");
            node.setHeaders(headers);
            node.setProperties(null);
        }

        Node duplicate = new NodeImpl(node);

        assertThat(duplicate.getProperties().isEmpty(), is(true));
        assertFalse(duplicate.getRevision().isPresent());
        assertThat(duplicate.getHeaders().size(), is(3));
        assertThat(duplicate.get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(duplicate.get(NodeHeader.KIND), is(equalTo("dir")));
        assertThat(duplicate.get(NodeHeader.PATH), is(equalTo("dir1")));
        assertTrue(duplicate.getContent().isEmpty());
    }

    @Test
    public void get_properties_should_return_empty_map() {
        NodeImpl node = new NodeImpl();
        assertNotNull(node.getProperties());
        assertTrue(node.getProperties().isEmpty());
    }

    @Test
    public void toString_works() {
        NodeImpl node = new NodeImpl();

        Map<NodeHeader, String> headers = new HashMap<>();
        headers.put(NodeHeader.MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(NodeHeader.COPY_FROM_PATH, "some/old/path.txt");
        headers.put(NodeHeader.COPY_FROM_REV, "2");
        headers.put(NodeHeader.SOURCE_MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(NodeHeader.ACTION, "add");
        headers.put(NodeHeader.KIND, "file");
        headers.put(NodeHeader.PATH, "new/path.txt");
        node.setHeaders(headers);

        assertThat(node.toString(), is(equalTo("add file new/path.txt eff2191c7e5abb19d79e8bcb2f1b7f38 -- copied from: some/old/path.txt@2 eff2191c7e5abb19d79e8bcb2f1b7f38")));
    }

    @Test
    public void toString_without_source_md5() {
        NodeImpl node = new NodeImpl();

        Map<NodeHeader, String> headers = new HashMap<>();
        headers.put(NodeHeader.MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(NodeHeader.COPY_FROM_PATH, "some/old/path.txt");
        headers.put(NodeHeader.COPY_FROM_REV, "2");
        headers.put(NodeHeader.ACTION, "add");
        headers.put(NodeHeader.KIND, "file");
        headers.put(NodeHeader.PATH, "new/path.txt");
        node.setHeaders(headers);

        assertThat(node.toString(), is(equalTo("add file new/path.txt eff2191c7e5abb19d79e8bcb2f1b7f38 -- copied from: some/old/path.txt@2")));
    }

    @Test
    public void toString_with_size() {
        NodeImpl node = new NodeImpl();

        Map<NodeHeader, String> headers = new HashMap<>();
        headers.put(NodeHeader.MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(NodeHeader.COPY_FROM_PATH, "some/old/path.txt");
        headers.put(NodeHeader.COPY_FROM_REV, "2");
        headers.put(NodeHeader.SOURCE_MD5, "eff2191c7e5abb19d79e8bcb2f1b7f38");
        headers.put(NodeHeader.ACTION, "add");
        headers.put(NodeHeader.KIND, "file");
        headers.put(NodeHeader.PATH, "new/path.txt");
        headers.put(NodeHeader.TEXT_CONTENT_LENGTH, "123456");
        node.setHeaders(headers);

        assertThat(node.toString(), is(equalTo("add file new/path.txt eff2191c7e5abb19d79e8bcb2f1b7f38 -- copied from: some/old/path.txt@2 eff2191c7e5abb19d79e8bcb2f1b7f38 Size: 123456 bytes")));
    }
}