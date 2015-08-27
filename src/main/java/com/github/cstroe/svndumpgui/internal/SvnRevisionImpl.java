package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnRevision;

import java.util.HashMap;
import java.util.Map;

public class SvnRevisionImpl implements SvnRevision {

    private final int number;
    private final Map<String, String> properties;

    public SvnRevisionImpl(int number) {
        this(number, null);
    }

    public SvnRevisionImpl(int number, String date) {
        this.number = number;
        properties = new HashMap<>();
        this.setProperty(DATE, date);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getProperty(String name) {
        return properties.get(name);
    }

    public void setProperty(String name, String value) {
        properties.put(name, value);
    }
}
