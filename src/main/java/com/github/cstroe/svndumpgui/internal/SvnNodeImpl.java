package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnNode;

public class SvnNodeImpl implements SvnNode {
    private String path;
    private String kind;
    private String action;
    private String md5;
    private String sha1;
    private byte[] content;
    private Integer copiedFromRevision;
    private String copiedFromPath;
    private String copiedFromMd5;
    private String copiedFromSha1;

    @Override
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    @Override
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    @Override
    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    @Override
    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    @Override
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    @Override
    public Integer getCopiedFromRevision() {
        return copiedFromRevision;
    }

    public void setCopiedFromRevision(Integer copiedFromRevision) {
        this.copiedFromRevision = copiedFromRevision;
    }

    @Override
    public String getCopiedFromPath() {
        return copiedFromPath;
    }

    public void setCopiedFromPath(String copiedFromPath) {
        this.copiedFromPath = copiedFromPath;
    }

    @Override
    public String getCopiedFromMd5() {
        return copiedFromMd5;
    }

    public void setCopiedFromMd5(String copiedFromMd5) {
        this.copiedFromMd5 = copiedFromMd5;
    }

    @Override
    public String getCopiedFromSha1() {
        return copiedFromSha1;
    }

    public void setCopiedFromSha1(String copiedFromSha1) {
        this.copiedFromSha1 = copiedFromSha1;
    }

    @Override
    public String toString() {
        if(kind != null) {
            return String.valueOf(action) + " " + String.valueOf(kind) + " " + String.valueOf(path);
        } else {
            return String.valueOf(action) + " " + String.valueOf(path);
        }
    }
}
