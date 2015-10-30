package com.github.cstroe.svndumpgui.api;

import java.io.OutputStream;

public interface RepositoryWriter extends RepositoryConsumer {
    void writeTo(OutputStream os);
}
