package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.Date;

public class SvnRevisionImpl implements SvnRevision {

    private final int number;
    private Date date;

    public SvnRevisionImpl(int number) {
        this(number, null);
    }

    public SvnRevisionImpl(int number, Date date) {
        this.number = number;
        this.date = date;
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public Date getDate() {
        return date;
    }
}
