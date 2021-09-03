package com.github.cstroe.svndumpgui.api;


public enum NodeHeader {

    PATH("Node-path: "),
    KIND("Node-kind: "),
    ACTION("Node-action: "),
    PROP_CONTENT_LENGTH("Prop-content-length: "),
    TEXT_CONTENT_LENGTH("Text-content-length: "),
    MD5("Text-content-md5: "),
    SHA1("Text-content-sha1: "),
    CONTENT_LENGTH("Content-length: "),
    COPY_FROM_REV("Node-copyfrom-rev: "),
    COPY_FROM_PATH("Node-copyfrom-path: "),
    SOURCE_MD5("Text-copy-source-md5: "),
    SOURCE_SHA1("Text-copy-source-sha1: "),

    ;

    private final String rawText;

    /**
     * @param rawText the text representation of this node header
     */
    NodeHeader(String rawText) {
        this.rawText = rawText;
    }

    @Override
    public String toString() {
        return rawText;
    }
}
