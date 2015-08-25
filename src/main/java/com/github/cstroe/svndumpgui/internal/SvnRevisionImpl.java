package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.Date;

public class SvnRevisionImpl implements SvnRevision {
    @Override
    public int getNumber() {
        return 0;
    }

    @Override
    public Date getDate() {
        return new Date();
    }
}
