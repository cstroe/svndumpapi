package com.github.cstroe.svndumpgui.internal.transform.replace;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.internal.NodeImpl;
import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class NodeSerializerTest {
    @Test
    public void hashes_are_returned() {
        Node node = new NodeImpl();
        node.getHeaders().put(NodeHeader.MD5, "test md5");
        node.getHeaders().put(NodeHeader.SHA1, "test sha1");

        Node reconstitutedNode = NodeSerializer.fromBytes(NodeSerializer.toBytes(node));
        assertThat(reconstitutedNode.get(NodeHeader.MD5), is(equalTo("test md5")));
        assertThat(reconstitutedNode.get(NodeHeader.SHA1), is(equalTo("test sha1")));

        Node twiceDistilled = NodeSerializer.fromBytes(NodeSerializer.toBytes(reconstitutedNode));
        assertThat(twiceDistilled.get(NodeHeader.MD5), is(equalTo("test md5")));
        assertThat(twiceDistilled.get(NodeHeader.SHA1), is(equalTo("test sha1")));
    }
}