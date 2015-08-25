package com.github.cstroe.svndumpgui.api;

import java.io.IOException;
import java.io.InputStream;

public interface SvnDumpParser {
    SvnDump parse(InputStream is) throws IOException;
}
