package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.generated.ParseException;
import junit.framework.ComparisonFailure;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

public class SvnDumpWriterImplTest {

    @Test
    public void write_empty_dump() throws ParseException, IOException {
        recreateDumpFile("dumps/empty.dump");
    }

    private void recreateDumpFile(String dumpFile) throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserTest.parse(dumpFile);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpWriter dumpWriter = new SvnDumpWriterImpl();

        dumpWriter.write(baos, dump);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dumpFile);

        assertEqual(s, bais);
    }

    // adapted from: http://stackoverflow.com/questions/4245863
    private void assertEqual(InputStream expectedStream, InputStream actualStream) throws IOException {
        byte[] buf1 = new byte[64 *1024];
        byte[] buf2 = new byte[64 *1024];
        boolean readingD2 = false;
        try {
            DataInputStream d2 = new DataInputStream(actualStream);
            long filePosition = 0;
            int len;
            while ((len = expectedStream.read(buf1)) > 0) {
                readingD2 = true;
                d2.readFully(buf2,0,len);
                readingD2 = false;
                for(int i=0;i<len;i++, filePosition++)
                    if(buf1[i] != buf2[i]) {
                        throw new ComparisonFailure("Streams differ.", new String(buf1), new String(buf2));
                    }
            }
            if(!(d2.read() < 0)) { // is the end of the second file also?
                throw new AssertionError("Actual stream is shorter than expected.");
            }
        } catch(EOFException ioe) {
            if(!readingD2) {
                throw new ComparisonFailure("Actual stream is longer than expected.", new String(buf1), new String(buf2));
            } else {
                throw new ComparisonFailure("Actual stream is shorter than expected.", new String(buf1), new String(buf2));
            }
        } finally {
            expectedStream.close();
            actualStream.close();
        }
    }
}