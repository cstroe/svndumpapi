package com.github.cstroe.svndumpgui.internal.utility;


import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.generated.TokenMgrError;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpSummary;

import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

//import com.github.cstroe.svndumpgui.internal.writer.SvnDumpDebug;

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
        //new SvnDumpDebug().after(writer);

        //InputStream is = new BufferedInputStream(new FileInputStream("/home/cosmin/Zoo/svndumpgui/svndump"));
        FastCharStream fastCharStream = new FastCharStream(new InputStreamReader(System.in, "US-ASCII"));
        SvnDumpFileParser parser = new SvnDumpFileParser(fastCharStream);
        //parser.setFileContentChunkSize(1024 * 1024 * 32);
        try {
            parser.Start(writer);
        } catch(TokenMgrError err) {
            System.err.println(err.getMessage());
            System.err.println("Stream position: " + fastCharStream.getStreamPosition());
        }

        System.out.flush();
    }
}
