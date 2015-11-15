package com.github.cstroe.svndumpgui.cli;

import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class CliConsumer {
    public static void main(final String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException {
        RepositoryWriter writer = new SvnDumpWriter();
        writer.writeTo(System.out);
        SvnDumpParser.consume(System.in, writer);
        System.out.flush();
    }
}
