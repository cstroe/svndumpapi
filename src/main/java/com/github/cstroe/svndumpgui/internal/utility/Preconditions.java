package com.github.cstroe.svndumpgui.internal.utility;

/**
 * Copied from Guava, instead of depending on full library.
 */
public class Preconditions {
    public static <T> T checkNotNull(T reference) {
        if (reference == null) {
            throw new NullPointerException();
        }
        return reference;
    }
}
