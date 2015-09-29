package com.github.cstroe.svndumpgui.internal.utility;


import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.transform.ConsumerChain;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpSummary;

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
        ConsumerChain chain = new ConsumerChain();

        SvnDumpWriter writer = new SvnDumpSummary();
        writer.writeTo(System.out);
        chain.add(writer);

        SvnDumpFileParser parser = new SvnDumpFileParser(new FastCharStream(new InputStreamReader(System.in, "ISO-8859-1")));
        parser.Start(chain);

        System.out.flush();
    }
}
