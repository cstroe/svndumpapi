package com.github.cstroe.svndumpgui.api;

import java.io.OutputStream;

public interface SvnDumpWriter extends SvnDumpConsumer {
    void writeTo(OutputStream os);
}
