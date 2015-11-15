package com.github.cstroe.svndumpgui.internal.writer;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.RepositoryImpl;
import com.github.cstroe.svndumpgui.internal.PreambleImpl;
import com.github.cstroe.svndumpgui.internal.RevisionImpl;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpParserDoppelganger;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.github.cstroe.svndumpgui.internal.utility.TestUtil.assertEqualStreams;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SvnDumpWriterTest {

    @Test
    public void no_uuid() throws IOException {
        Repository dump = new RepositoryImpl();
        dump.setPreamble(new PreambleImpl());
        Revision r0 = new RevisionImpl(0);
        dump.getRevisions().add(r0);

        RepositoryWriter writer = new SvnDumpWriter();
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        writer.writeTo(os);
        SvnDumpParserDoppelganger.consumeWithoutChaining(dump, writer);
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
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        SvnDumpParser.consume(TestUtil.openResource("dumps/simple_branch_and_merge.dump"), dumpInMemory);
        Repository dump = dumpInMemory.getRepo();

        RepositoryWriter writer = new SvnDumpWriter();
        ByteArrayOutputStream firstStream = new ByteArrayOutputStream();
        writer.writeTo(firstStream);
        SvnDumpParserDoppelganger.consumeWithoutChaining(dump, writer);

        final InputStream s1 = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        assertEqualStreams(s1, new ByteArrayInputStream(firstStream.toByteArray()));

        Repository readDump = SvnDumpFileParserTest.parse(new ByteArrayInputStream(firstStream.toByteArray()));

        ByteArrayOutputStream secondStream = new ByteArrayOutputStream();
        writer.writeTo(secondStream);
        SvnDumpParserDoppelganger.consumeWithoutChaining(readDump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/simple_branch_and_merge.dump");

        assertEqualStreams(s, new ByteArrayInputStream(secondStream.toByteArray()));
    }

    private void recreateDumpFile(String dumpFile) throws ParseException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RepositoryWriter dumpWriter = new SvnDumpWriter();
        dumpWriter.writeTo(baos);
        SvnDumpParser.consume(TestUtil.openResource(dumpFile), dumpWriter);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dumpFile);

        assertEqualStreams(s, bais);
    }

    @Test
    public void recreate_svn_replace() throws ParseException, IOException {
        recreateDumpFile("dumps/svn_replace.dump");
    }

    @Test
    public void recreate_utf8_log_message() throws ParseException, IOException {
        recreateDumpFile("dumps/utf8_log_message.dump");
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_consume_null_chunks(){
        RepositoryWriter writer = new SvnDumpWriter();
        writer.consume((ContentChunk)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dont_consume_chunks_with_null_content(){
        RepositoryWriter writer = new SvnDumpWriter();
        writer.consume(new ContentChunk() {
            @Override
            public byte[] getContent() {
                return null;
            }

            @Override
            public void setContent(byte[] content) {}
        });
    }
}