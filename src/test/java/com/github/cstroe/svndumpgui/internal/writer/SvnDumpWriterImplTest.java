package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpPreambleImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import junit.framework.ComparisonFailure;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SvnDumpWriterImplTest {

    @Test
    public void no_uuid() throws IOException {
        SvnDump dump = new SvnDumpImpl();
        dump.setPreamble(new SvnDumpPreambleImpl());
        SvnRevision r0 = new SvnRevisionImpl(0);
        dump.getRevisions().add(r0);

        SvnDumpWriter writer = new SvnDumpWriterImpl();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.writeTo(os);
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(dump, writer);
        assertThat(os.toString(), is(equalTo("SVN-fs-dump-format-version: 2\n\n\nRevision-number: 0\nProp-content-length: 10\nContent-length: 10\n\nPROPS-END\n\n")));
    }

    @Test
    public void write_empty_dump() throws ParseException, IOException {
        recreateDumpFile("dumps/empty.dump");
    }

    @Test
    public void write_dump_with_one_commit() throws ParseException, IOException {
        recreateDumpFile("dumps/firstcommit.dump");
    }

    @Test
    public void write_dump_with_file_content() throws ParseException, IOException {
        recreateDumpFile("dumps/add_file.dump");
    }

    @Test
    public void write_dump_with_different_node_order() throws ParseException, IOException {
        recreateDumpFile("dumps/different_node_order.dump");
    }

    @Test
    public void write_dump_with_different_node_order2() throws ParseException, IOException {
        recreateDumpFile("dumps/different_node_order2.dump");
    }

    @Test
    public void write_dump_with_optional_node_properties() throws ParseException, IOException {
        recreateDumpFile("dumps/add_file_no_node_properties.dump");
    }

    @Test
    public void write_dump_with_binary_file_commit() throws ParseException, IOException {
        recreateDumpFile("dumps/binary_commit.dump");
    }

    @Test
    public void write_dump_rename() throws ParseException, IOException {
        recreateDumpFile("dumps/svn_rename.dump");
    }

    @Test
    public void write_no_copy_hashes() throws ParseException, IOException {
        recreateDumpFile("dumps/svn_rename_no_copy_hashes.dump");
    }

    @Test
    public void write_directory_nodes() throws ParseException, IOException {
        recreateDumpFile("dumps/svn_add_directory.dump");
    }

    @Test
    public void write_delete_nodes() throws ParseException, IOException {
        recreateDumpFile("dumps/svn_delete_file.dump");
    }

    @Test
    public void other_tests() throws ParseException, IOException {
        // these already pass, but thought they might be useful
        recreateDumpFile("dumps/svn_delete_with_add.dump");
        recreateDumpFile("dumps/svn_copy_file.dump");
        recreateDumpFile("dumps/svn_multi_dir_delete.dump");
        recreateDumpFile("dumps/svn_multi_file_delete.dump");
    }

    @Test
    public void rewrite_file() throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserDoppelganger.parse("dumps/simple_branch_and_merge.dump");
        SvnDumpWriter writer = new SvnDumpWriterImpl();
        ByteArrayOutputStream firstStream = new ByteArrayOutputStream();
        writer.writeTo(firstStream);
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(dump, writer);

        SvnDump readDump = SvnDumpFileParserTest.parse(new ByteArrayInputStream(firstStream.toByteArray()));

        ByteArrayOutputStream secondStream = new ByteArrayOutputStream();
        writer.writeTo(secondStream);
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(readDump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        SvnDumpWriterImplTest.assertEqualStreams(s, new ByteArrayInputStream(secondStream.toByteArray()));
    }

    private void recreateDumpFile(String dumpFile) throws ParseException, IOException {
        SvnDump dump = SvnDumpFileParserDoppelganger.parse(dumpFile);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpWriter dumpWriter = new SvnDumpWriterImpl();
        dumpWriter.writeTo(baos);
        SvnDumpFileParserDoppelganger.consume(dump, dumpWriter);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dumpFile);

        assertEqualStreams(s, bais);
    }

    // adapted from: http://stackoverflow.com/questions/4245863
    public static void assertEqualStreams(InputStream expectedStream, InputStream actualStream) throws IOException {
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
                        throw new ComparisonFailure("Streams differ." + buf1[i] + " != " + buf2[i], new String(buf1), new String(buf2));
                    }
            }
            int d2r = d2.read();
            if(!(d2r < 0)) { // is the end of the second file also?
                throw new ComparisonFailure("Actual stream is longer than expected. (The extra character is tacked on at the end)",
                        new String(buf1), new String(buf2) + String.valueOf((char)d2r));
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