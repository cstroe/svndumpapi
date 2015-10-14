package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnDumpImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpPreambleImpl;
import com.github.cstroe.svndumpgui.internal.SvnRevisionImpl;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.google.common.io.ByteStreams;
import junit.framework.ComparisonFailure;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

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
        SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/simple_branch_and_merge.dump"), dumpInMemory);
        SvnDump dump = dumpInMemory.getDump();

        SvnDumpWriter writer = new SvnDumpWriterImpl();
        ByteArrayOutputStream firstStream = new ByteArrayOutputStream();
        writer.writeTo(firstStream);
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(dump, writer);

        final InputStream s1 = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        assertEqualStreams(s1, new ByteArrayInputStream(firstStream.toByteArray()));

        SvnDump readDump = SvnDumpFileParserTest.parse(new ByteArrayInputStream(firstStream.toByteArray()));

        ByteArrayOutputStream secondStream = new ByteArrayOutputStream();
        writer.writeTo(secondStream);
        SvnDumpFileParserDoppelganger.consumeWithoutChaining(readDump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        assertEqualStreams(s, new ByteArrayInputStream(secondStream.toByteArray()));
    }

    private void recreateDumpFile(String dumpFile) throws ParseException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpWriter dumpWriter = new SvnDumpWriterImpl();
        dumpWriter.writeTo(baos);
        SvnDumpFileParser.consume(TestUtil.openResource(dumpFile), dumpWriter);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dumpFile);

        assertEqualStreams(s, bais);
    }

    public static void assertEqualStreams(InputStream expected, InputStream actual) throws IOException {
        byte[] expectedBytes = ByteStreams.toByteArray(expected);
        byte[] actualBytes = ByteStreams.toByteArray(actual);

        if(!Arrays.equals(expectedBytes, actualBytes)) {
            throw new ComparisonFailure("Streams differ.", new String(expectedBytes), new String(actualBytes));
        }
    }

    @Test
    public void recreate_svn_replace() throws ParseException, IOException {
        recreateDumpFile("dumps/svn_replace.dump");
    }

    @Test
    public void recreate_utf8_log_message() throws ParseException, IOException {
        recreateDumpFile("dumps/utf8_log_message.dump");
    }
}