package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.*;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpSummary;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertNotNull;

public class AMDump {


    @Test
    @Ignore
    public void should_parse_am_repo() throws ParseException, NoSuchAlgorithmException, IOException {
        final InputStream s = new FileInputStream("/home/cosmin/Desktop/AgreementMaker-GitHub-Conversion/onestep.dump");
        SvnDumpFileParser parser = new SvnDumpFileParser(s, "ISO-8859-1");
        SvnDump dump = parser.Start();

        assertNotNull(dump);

        FileOutputStream fos = new FileOutputStream("/tmp/am_dump.log");
        SvnDumpWriter summaryWriter = new SvnDumpSummary();
        summaryWriter.write(fos, dump);
    }
}
