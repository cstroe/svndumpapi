package com.github.cstroe.svndumpgui.internal.transform.replace;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class NodeSerializer {
    static byte[] toBytes(Node node) {
        return (node.get(NodeHeader.MD5) + ":" + node.get(NodeHeader.SHA1)).getBytes();
    }

    static Node fromBytes(byte[] bytes) {
        String[] hashes = new String(bytes).split(":");

        Node zombieNode = new Node() {
            private Map<NodeHeader, String> headers = new LinkedHashMap<>();

            @Override
            public Optional<Revision> getRevision() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setRevision(Revision revision) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<NodeHeader, String> getHeaders() {
                return headers;
            }

            @Override
            public void setHeaders(Map<NodeHeader, String> headers) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Map<String, String> getProperties() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setProperties(Map<String, String> properties) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<ContentChunk> getContent() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void addFileContentChunk(ContentChunk chunk) {
                throw new UnsupportedOperationException();
            }

            @Override
            public String get(NodeHeader header) {
                return headers.get(header);
            }
        };

        zombieNode.getHeaders().put(NodeHeader.MD5, hashes[0]);
        zombieNode.getHeaders().put(NodeHeader.SHA1, hashes[1]);
        return zombieNode;
    }
}
