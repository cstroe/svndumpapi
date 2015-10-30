package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.Preamble;

public class PreambleImpl implements Preamble {
    private String uuid;

    public PreambleImpl() {}

    public PreambleImpl(Preamble preamble) {
        this.uuid = preamble.getUUID();
    }

    public PreambleImpl(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    @Override
    public String toString() {
        if(uuid == null) {
            return "PreambleImpl";
        } else {
            return "PreambleImpl " + uuid;
        }
    }
}
