package com.github.cstroe.svndumpgui.api;

public interface SvnVisitable {
    void accept(SvnDumpConsumer consumer);
}
