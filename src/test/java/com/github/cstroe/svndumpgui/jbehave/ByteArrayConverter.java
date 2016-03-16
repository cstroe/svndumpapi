package com.github.cstroe.svndumpgui.jbehave;

import org.jbehave.core.steps.ParameterConverters;

import java.lang.reflect.Type;

public class ByteArrayConverter implements ParameterConverters.ParameterConverter {

    @Override
    public boolean accept(Type type) {
        return type.equals(byte[].class);
    }

    @Override
    public Object convertValue(String value, Type type) {
        return value.getBytes();
    }
}
