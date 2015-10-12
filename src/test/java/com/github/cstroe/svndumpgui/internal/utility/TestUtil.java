package com.github.cstroe.svndumpgui.internal.utility;

import java.io.InputStream;

public class TestUtil {
    public static InputStream openResource(String file) {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(file);
        assert s != null;
        return s;
    }
}
