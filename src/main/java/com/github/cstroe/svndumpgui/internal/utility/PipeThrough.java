package com.github.cstroe.svndumpgui.internal.utility;


import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpWriterImpl;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

/**
 * Use this from a command line interface to pipe your dump into svndumpgui,
 * and optionally out to something else.
 *
 * Add your SvnDumpConsumers to the chain to achieve the desired output.
 */
public class PipeThrough {

    public static void main(final String[] args) throws ParseException, UnsupportedEncodingException {
        SvnDumpWriter writer = new SvnDumpWriterImpl();
        writer.writeTo(System.out);

        SvnDumpFileParser parser = new SvnDumpFileParser(new FastCharStream(new InputStreamReader(System.in, "ISO-8859-1")));
        parser.Start(writer);

        System.out.flush();
    }
}
