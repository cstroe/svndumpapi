package com.github.cstroe.svndumpgui.internal.transform.property;

import java.util.function.Function;

public class EnsureTrailingLinefeed implements Function<String, String> {
    @Override
    public String apply(String s) {
        if(!s.endsWith("\n")) {
            return s + "\n";
        } else {
            return s;
        }
    }
}
