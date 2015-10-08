package com.github.cstroe.svndumpgui.cli;

import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImpl;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class CliConsumer {
    public static void main(final String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException {
        SvnDumpWriter writer = new SvnDumpWriterImpl();
        writer.writeTo(System.out);
        SvnDumpFileParser.consume(System.in, writer);
        System.out.flush();
    }
}
