package com.github.cstroe.svndumpgui.api;

import java.io.IOException;
import java.io.OutputStream;

public interface SvnDumpWriter {
    void write(OutputStream os, SvnDump dump) throws IOException;
}
