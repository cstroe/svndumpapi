package com.github.cstroe.svndumpgui.internal.utility;


import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpSummary;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

/**
 * Use this from a command line interface to pipe your dump into svndumpgui,
 * and optionally out to something else.
 *
 * Add your SvnDumpConsumers to the chain to achieve the desired output.
 */
public class PipeThrough {

    public static void main(final String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException {
        SvnDumpWriter writer = new SvnDumpSummary();
        writer.writeTo(System.out);
        SvnDumpFileParser.consume(System.in, writer);
        System.out.flush();
    }
}
