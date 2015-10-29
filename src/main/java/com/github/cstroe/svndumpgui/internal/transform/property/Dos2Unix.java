package com.github.cstroe.svndumpgui.internal.transform.property;

import java.util.function.Function;

/**
 * Convert string to Unix style linefeed and make sure the string ends in a line feed.
 */
public class Dos2Unix implements Function<String, String> {
    @Override
    public String apply(String s) {
        String newString = s.replace("\r\n", "\n");
        if(!newString.endsWith("\n")) {
            return newString + "\n";
        } else {
            return newString;
        }
    }
}
