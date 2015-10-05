package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDumpConsumer;
import com.github.cstroe.svndumpgui.api.SvnDumpPreamble;

public class SvnDumpPreambleImpl implements SvnDumpPreamble {
    private String uuid;

    public SvnDumpPreambleImpl() {}

    public SvnDumpPreambleImpl(SvnDumpPreamble preamble) {
        if(preamble != null) {
            this.uuid = preamble.getUUID();
        }
    }

    public SvnDumpPreambleImpl(String uuid) {
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
    public void accept(SvnDumpConsumer consumer) {
        consumer.consume(this);
    }

    @Override
    public String toString() {
        if(uuid == null) {
            return "SvnDumpPreambleImpl";
        } else {
            return "SvnDumpPreambleImpl " + uuid;
        }
    }
}
