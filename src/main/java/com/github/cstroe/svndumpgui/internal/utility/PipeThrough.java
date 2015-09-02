package com.github.cstroe.svndumpgui.internal.utility;


import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpWriterImpl;
import com.github.cstroe.svndumpgui.internal.transform.ConsumerChain;

/**
 * Use this from a command line interface to pipe your dump into svndumpgui,
 * and optionally out to something else.
 *
 * Add your SvnDumpConsumers to the chain to achieve the desired output.
 */
public class PipeThrough {

    public static void main(final String[] args) throws ParseException {
        ConsumerChain chain = new ConsumerChain();

        SvnDumpWriter writer = new SvnDumpWriterImpl();
        writer.writeTo(System.out);
        chain.add(writer);

        SvnDumpFileParser parser = new SvnDumpFileParser(System.in, "ISO-8859-1");
        parser.Start(chain);

        System.out.flush();
    }
}
