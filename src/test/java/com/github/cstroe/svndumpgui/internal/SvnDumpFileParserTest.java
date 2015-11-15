package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Preamble;
import com.github.cstroe.svndumpgui.api.Property;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.CharStream;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpCharStream;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.Sequence;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class SvnDumpFileParserTest {

    public static Repository parse(String dumpFile) throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(dumpFile);
        assert s != null;
        return parse(s);
    }

    public static Repository parse(InputStream is) throws ParseException {
        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(is));
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        parser.Start(dumpInMemory);
        return dumpInMemory.getRepo();
    }

    /**
     * @return The SvnDump after it's been modified by the consumer.
     */
    public static Repository consume(String dumpFile, RepositoryConsumer consumer) throws ParseException {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dumpFile);

        return consume(is, consumer);
    }

    /**
     * @return The SvnDump after it's been modified by the consumer.
     */
    public static Repository consume(InputStream is, RepositoryConsumer consumer) throws ParseException {
        RepositoryInMemory saveDump = new RepositoryInMemory();
        consumer.continueTo(saveDump);

        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(is));
        parser.Start(consumer);

        return saveDump.getRepo();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simple_property_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/simple_property.fragment");

        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(s));

        RevisionImpl revision = new RevisionImpl(0);
        Map properties = parser.Property();
        revision.setProperties(properties);

        assertNotNull(revision.get(Property.DATE));
        assertThat(revision.get(Property.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void empty_revision_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/empty_revision.fragment");

        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(s));
        Revision revision = parser.Revision();

        assertNotNull(revision);
        assertThat(revision.getNumber(), is(0));
        assertThat(revision.get(Property.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void should_parse_uuid() throws IOException, ParseException {
        Repository dump = parse("dumps/empty.dump");

        assertNotNull(dump);
        assertThat(dump.getPreamble().getUUID(), is(equalTo("0c9743f5-f757-4bed-a5b3-acbcba4d645b")));
    }

    @Test
    public void should_parse_empty_dump() throws IOException, ParseException {
        Repository dump = parse("dumps/empty.dump");

        assertNotNull(dump);

        List<Revision> revisionList = dump.getRevisions();

        assertThat("There should be a revision present", revisionList.size(), is(1));

        Revision firstRevision = revisionList.get(0);

        assertThat(firstRevision.getNumber(), is(0));
        assertNotNull(firstRevision.get(Property.DATE));
        assertThat(firstRevision.get(Property.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void should_parse_one_commit() throws ParseException {
        Repository dump = parse("dumps/firstcommit.dump");

        assertThat("The repository dump contains two revisions.", dump.getRevisions().size(), is(2));

        assertThat(dump.getRevisions().get(0).getNumber(), is(0));
        assertThat(dump.getRevisions().get(0).get(Property.DATE), is(equalTo("2015-10-14T08:38:32.304465Z")));

        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).get(Property.DATE), is(equalTo("2015-10-14T08:38:32.462239Z")));
        assertThat(dump.getRevisions().get(1).get(Property.AUTHOR), is(equalTo("cosmin")));
        assertThat(dump.getRevisions().get(1).get(Property.LOG), is(equalTo("Added a first file.")));

        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        Node fileNode = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(fileNode.get(NodeHeader.PATH), is(equalTo("firstFile.txt")));
        assertThat(fileNode.get(NodeHeader.KIND), is(equalTo("file")));
        assertThat(fileNode.get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(fileNode.get(NodeHeader.MD5), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
        assertThat(fileNode.get(NodeHeader.SHA1), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
    }

    @Test
    public void should_parse_file_content() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/add_file.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        Node readmeTxt = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(readmeTxt.get(NodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(readmeTxt.get(NodeHeader.KIND), is(equalTo("file")));
        assertThat(readmeTxt.get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(readmeTxt.get(NodeHeader.MD5), is(equalTo("4221d002ceb5d3c9e9137e495ceaa647")));
        assertThat(readmeTxt.get(NodeHeader.SHA1), is(equalTo("804d716fc5844f1cc5516c8f0be7a480517fdea2")));
        assertThat(new String(readmeTxt.getByteContent()), is(equalTo("this is a test file\n")));

        assertThat(TestUtil.md5sum(readmeTxt.getByteContent()), is(equalTo(readmeTxt.get(NodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getByteContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(NodeHeader.SHA1))));
    }

    @Test
    public void should_allow_for_optional_properties_on_nodes() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/add_file_no_node_properties.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        Node readmeTxt = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(readmeTxt.get(NodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(readmeTxt.get(NodeHeader.KIND), is(equalTo("file")));
        assertThat(readmeTxt.get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(readmeTxt.get(NodeHeader.MD5), is(equalTo("4221d002ceb5d3c9e9137e495ceaa647")));
        assertThat(readmeTxt.get(NodeHeader.SHA1), is(equalTo("804d716fc5844f1cc5516c8f0be7a480517fdea2")));
        assertThat(new String(readmeTxt.getByteContent()), is(equalTo("this is a test file\n")));

        assertThat(TestUtil.md5sum(readmeTxt.getByteContent()), is(equalTo(readmeTxt.get(NodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getByteContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(NodeHeader.SHA1))));
    }

    private String sha1sum(byte[] digest) {
        return TestUtil.toHex(digest, 40);
    }

    /**
     * We should be flexible with the order of the Node headers.
     *
     * For example, sometimes "Node-path" come before "Node-kind", other
     * times it's the reverse.  We should parse both correctly.
     */
    @Test
    public void should_parse_nodes_with_different_ordering_of_headers() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/different_node_order.dump");

        assertThat(dump.getRevisions().size(), is(2));

        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.PATH), is("AM-Core"));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.KIND), is("dir"));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.ACTION), is("add"));
    }

    /**
     * The ordering of all the node headers should not matter.
     */
    @Test
    public void should_parse_nodes_with_different_ordering_of_headers2() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/different_node_order2.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        Node node = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(node.get(NodeHeader.PATH), is("AM-Core"));
        assertThat(node.get(NodeHeader.KIND), is("dir"));
        assertThat(node.get(NodeHeader.ACTION), is("add"));
        assertThat(node.get(NodeHeader.SHA1), is("53ff16933cc0ec0077ea0d5f848ef0fd61440c27"));
    }

    @Test
    public void should_parse_binary_file() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/binary_commit.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).get(Property.LOG), is("Adding binary file."));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        Node fileBin = dump.getRevisions().get(1).getNodes().get(0);

        assertThat(fileBin.getProperties().get(Property.MIMETYPE), is(equalTo("application/octet-stream")));

        assertThat(fileBin.getByteContent().length, is(1024));
        assertThat(fileBin.get(NodeHeader.PATH), is("file.bin"));

        assertThat(TestUtil.md5sum(fileBin.getByteContent()), is(equalTo(fileBin.get(NodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(fileBin.getByteContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(fileBin.get(NodeHeader.SHA1))));
    }

    @Test
    public void should_parse_svn_mv_operations() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/svn_rename.dump");

        assertThat(dump.getRevisions().size(), is(3));

        // validate the revision in which we create the file
        Revision createFileRevision = dump.getRevisions().get(1);
        assertThat(createFileRevision.getNumber(), is(1));
        assertThat(createFileRevision.get(Property.LOG), is(equalTo("Committed README.txt")));
        assertThat(createFileRevision.getNodes().size(), is(1));

        Node readmeTxt = createFileRevision.getNodes().get(0);

        assertThat(readmeTxt.getByteContent().length, is(20));
        assertThat(readmeTxt.get(NodeHeader.PATH), is("README.txt"));

        assertThat(TestUtil.md5sum(readmeTxt.getByteContent()), is(equalTo(readmeTxt.get(NodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getByteContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(NodeHeader.SHA1))));

        // validate the revision in which we rename the file
        Revision moveFileRevision = dump.getRevisions().get(2);
        assertThat(moveFileRevision.get(Property.LOG), is(equalTo("Renamed README.txt to README-new.txt")));
        assertThat(moveFileRevision.getNodes().size(), is(2));

        Node newFileNode = moveFileRevision.getNodes().get(0);
        assertThat(newFileNode.get(NodeHeader.PATH), is(equalTo("README-new.txt")));
        assertThat(newFileNode.get(NodeHeader.KIND), is(equalTo("file")));
        assertThat(newFileNode.get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(newFileNode.getContent().size(), is(0));
        assertThat(newFileNode.get(NodeHeader.COPY_FROM_REV), is(equalTo("1")));
        assertThat(newFileNode.get(NodeHeader.COPY_FROM_PATH), is(equalTo("README.txt")));
        assertThat(newFileNode.get(NodeHeader.SOURCE_MD5), is(equalTo(readmeTxt.get(NodeHeader.MD5))));
        assertThat(newFileNode.get(NodeHeader.SOURCE_SHA1), is(equalTo(readmeTxt.get(NodeHeader.SHA1))));

        Node oldFileNode  = moveFileRevision.getNodes().get(1);
        assertThat(oldFileNode.get(NodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(oldFileNode.get(NodeHeader.ACTION), is(equalTo("delete")));
    }

    @Test
    public void should_parse_svn_mv_operations_without_copy_hashes() throws ParseException, NoSuchAlgorithmException {
        Repository dump = parse("dumps/svn_rename_no_copy_hashes.dump");

        assertThat(dump.getRevisions().size(), is(3));

        // validate the revision in which we create the file
        Revision createFileRevision = dump.getRevisions().get(1);
        assertThat(createFileRevision.getNumber(), is(1));
        assertThat(createFileRevision.get(Property.LOG), is(equalTo("Committed README.txt")));
        assertThat(createFileRevision.getNodes().size(), is(1));

        Node readmeTxt = createFileRevision.getNodes().get(0);

        assertThat(readmeTxt.getByteContent().length, is(20));
        assertThat(readmeTxt.get(NodeHeader.PATH), is("README.txt"));

        assertThat(TestUtil.md5sum(readmeTxt.getByteContent()), is(equalTo(readmeTxt.get(NodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getByteContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(NodeHeader.SHA1))));

        // validate the revision in which we rename the file
        Revision moveFileRevision = dump.getRevisions().get(2);
        assertThat(moveFileRevision.get(Property.LOG), is(equalTo("Renamed README.txt to README-new.txt")));
        assertThat(moveFileRevision.getNodes().size(), is(2));

        Node newFileNode = moveFileRevision.getNodes().get(0);
        assertThat(newFileNode.get(NodeHeader.PATH), is(equalTo("README-new.txt")));
        assertThat(newFileNode.get(NodeHeader.KIND), is(equalTo("file")));
        assertThat(newFileNode.get(NodeHeader.ACTION), is(equalTo("add")));
        assertThat(newFileNode.getContent().size(), is(0));
        assertThat(newFileNode.get(NodeHeader.COPY_FROM_REV), is(equalTo("1")));
        assertThat(newFileNode.get(NodeHeader.COPY_FROM_PATH), is(equalTo("README.txt")));

        Node oldFileNode  = moveFileRevision.getNodes().get(1);
        assertThat(oldFileNode.get(NodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(oldFileNode.get(NodeHeader.ACTION), is(equalTo("delete")));
    }

    @Test
    public void should_parse_directory() throws ParseException {
        Repository dump = parse("dumps/svn_add_directory.dump");

        assertThat(dump.getRevisions().size(), is(3));

        Revision r1 = dump.getRevisions().get(1);

        assertThat(r1.getNumber(), is(1));
        assertThat(r1.getNodes().size(), is(1));

        Node dir = r1.getNodes().get(0);
        assertThat(dir.get(NodeHeader.KIND), is(equalTo("dir")));
        assertThat(dir.get(NodeHeader.PATH), is(equalTo("testdir")));
        assertThat(dir.get(NodeHeader.ACTION), is(equalTo("add")));

        Revision r2 = dump.getRevisions().get(2);

        assertThat(r2.getNumber(), is(2));
        assertThat(r2.getNodes().size(), is(1));

        Node file = r2.getNodes().get(0);
        assertThat(file.get(NodeHeader.KIND), is(equalTo("file")));
        assertThat(file.get(NodeHeader.PATH), is(equalTo("testdir/README.txt")));
        assertThat(file.get(NodeHeader.ACTION), is(equalTo("add")));
    }

    @Test
    public void should_parse_file_deletion() throws ParseException {
        Repository dump = parse("dumps/svn_delete_file.dump");

        assertThat(dump.getRevisions().size(), is(4));

        Revision r2 = dump.getRevisions().get(2);

        assertThat(r2.getNumber(), is(2));
        assertThat(r2.getNodes().size(), is(1));

        Node fileDelete = r2.getNodes().get(0);
        assertNull(fileDelete.get(NodeHeader.KIND));
        assertThat(fileDelete.get(NodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(fileDelete.get(NodeHeader.ACTION), is(equalTo("delete")));
    }

    @Test
    public void should_parse_property_setting() throws ParseException {
        Repository dump = parse("dumps/property_change_on_file.dump");

        assertThat(dump.getRevisions().size(), is(4));

        Revision r2 = dump.getRevisions().get(2);
        assertThat(r2.getNodes().size(), is(1));
        Node node = r2.getNodes().get(0);
        assertThat(node.get(NodeHeader.PATH), is(equalTo("test.txt")));
        assertThat(node.getProperties().get("someproperty"), is(equalTo("value")));
    }

    @Test
    public void should_parse_extra_newline_in_log_message() throws ParseException {
        Repository dump = parse("dumps/extra_newline_in_log_message.dump");

        assertThat(dump.getRevisions().size(), is(2));

        Revision r1 = dump.getRevisions().get(1);
        assertThat(r1.getNodes().size(), is(1));
        Node node = r1.getNodes().get(0);
        assertThat(node.get(NodeHeader.PATH), is(equalTo("test.txt")));
    }

    @Test
    public void should_parse_property_change_on_root() throws ParseException {
        Repository dump = parse("dumps/property_change_on_root.dump");

        assertThat(dump.getRevisions().size(), is(2));

        Revision r1 = dump.getRevisions().get(1);
        assertThat(r1.getNodes().size(), is(1));
        Node node = r1.getNodes().get(0);
        assertThat(node.get(NodeHeader.PATH), is(equalTo("")));
        assertThat(node.getProperties().get("someproperty"), is(equalTo("value")));
    }

    @Test
    public void should_parse_file_content_via_file_content_chunks() throws ParseException {
        Mockery context = new Mockery();

        RepositoryConsumer consumer = context.mock(RepositoryConsumer.class, "consumer1");

        final Sequence consumerSequence = context.sequence("consumerSequence");
        context.checking(new Expectations() {{
            oneOf(consumer).consume(with(any(Preamble.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).endRevision(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Node.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(ContentChunk.class)));
            inSequence(consumerSequence);
            oneOf(consumer).endChunks();
            inSequence(consumerSequence);
            oneOf(consumer).endNode(with(any(Node.class)));
            inSequence(consumerSequence);
            oneOf(consumer).endRevision(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).finish();
            inSequence(consumerSequence);
        }});

        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/add_file.dump");
        SvnDumpParser.consume(is, consumer);
    }

    @Test
    public void should_respect_file_content_chunk_size() throws UnsupportedEncodingException, ParseException {
        final int fileContentChunkSize = 64;
        RepositoryImpl dump = new RepositoryImpl();
        {
            dump.setPreamble(new PreambleImpl("903a69a2-8256-45e6-a9dc-d9a846114b23"));
            Revision r0 = new RevisionImpl(0);
            dump.addRevision(r0);
            Revision r1 = new RevisionImpl(1);
            Node n1_1 = new NodeImpl(r1);
            n1_1.getHeaders().put(NodeHeader.ACTION, "add");
            n1_1.getHeaders().put(NodeHeader.KIND, "file");
            n1_1.getHeaders().put(NodeHeader.PATH, "file1");
            n1_1.getHeaders().put(NodeHeader.TEXT_CONTENT_LENGTH, "256");
            n1_1.getHeaders().put(NodeHeader.PROP_CONTENT_LENGTH, "56");
            n1_1.getProperties().put(Property.DATE, "2015-08-27T13:56:55.851461Z");
            byte[] content = new byte[256];
            for (int i = 0; i < 256; i++) {
                content[i] = 'a';
            }
            ContentChunk chunk = new ContentChunkImpl(content);
            n1_1.addFileContentChunk(chunk);
            r1.addNode(n1_1);
            dump.addRevision(r1);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpWriter writer = new SvnDumpWriter();
        writer.writeTo(baos);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(bais));
        parser.setFileContentChunkSize(fileContentChunkSize);

        Mockery context = new Mockery();

        RepositoryConsumer consumer = context.mock(RepositoryConsumer.class, "consumer1");

        Sequence consumerSequence = context.sequence("consumerSequence");

        context.checking(new Expectations() {{
            oneOf(consumer).consume(with(any(Preamble.class))); inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(consumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Node.class))); inSequence(consumerSequence);
            // 256 / 64 = 4 chunks
            oneOf(consumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(ContentChunk.class))); inSequence(consumerSequence);
            oneOf(consumer).endChunks(); inSequence(consumerSequence);
            oneOf(consumer).endNode(with(any(Node.class))); inSequence(consumerSequence);
            oneOf(consumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(consumer).finish(); inSequence(consumerSequence);
        }});

        parser.Start(consumer);

        // actually record the FileContentChunks this time
        bais = new ByteArrayInputStream(baos.toByteArray());
        parser = new SvnDumpParser(new SvnDumpCharStream(bais));
        parser.setFileContentChunkSize(fileContentChunkSize);
        RepositoryInMemory inMemoryDump = new RepositoryInMemory();
        parser.Start(inMemoryDump);

        List<ContentChunk> chunks = inMemoryDump.getRepo().getRevisions().get(1).getNodes().get(0).getContent();
        assertThat(chunks.size(), is(4));
        assertThat(chunks.get(0).getContent().length, is(64));
        assertThat(chunks.get(1).getContent().length, is(64));
        assertThat(chunks.get(2).getContent().length, is(64));
        assertThat(chunks.get(3).getContent().length, is(64));
    }

    @Test
    public void should_respect_file_content_chunk_size_with_short_chunk_at_end() throws UnsupportedEncodingException, ParseException {
        final int fileContentChunkSize = 100;
        RepositoryImpl dump = new RepositoryImpl();
        {
            dump.setPreamble(new PreambleImpl("903a69a2-8256-45e6-a9dc-d9a846114b23"));
            Revision r0 = new RevisionImpl(0);
            dump.addRevision(r0);
            Revision r1 = new RevisionImpl(1);
            Node n1_1 = new NodeImpl(r1);
            n1_1.getHeaders().put(NodeHeader.ACTION, "add");
            n1_1.getHeaders().put(NodeHeader.KIND, "file");
            n1_1.getHeaders().put(NodeHeader.PATH, "file1");
            n1_1.getHeaders().put(NodeHeader.TEXT_CONTENT_LENGTH, "256");
            n1_1.getHeaders().put(NodeHeader.PROP_CONTENT_LENGTH, "56");
            n1_1.getProperties().put(Property.DATE, "2015-08-27T13:56:55.851461Z");
            byte[] content = new byte[256];
            for (int i = 0; i < 256; i++) {
                content[i] = 'a';
            }
            ContentChunk chunk = new ContentChunkImpl(content);
            n1_1.addFileContentChunk(chunk);
            r1.addNode(n1_1);
            dump.addRevision(r1);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SvnDumpWriter writer = new SvnDumpWriter();
        writer.writeTo(baos);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(bais));
        parser.setFileContentChunkSize(fileContentChunkSize);

        Mockery context = new Mockery();

        RepositoryConsumer consumer = context.mock(RepositoryConsumer.class, "consumer1");

        Sequence consumerSequence = context.sequence("consumerSequence");

        context.checking(new Expectations() {{
            oneOf(consumer).consume(with(any(Preamble.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).endRevision(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(Node.class)));
            inSequence(consumerSequence);
            // 256 / 64 = 4 chunks
            oneOf(consumer).consume(with(any(ContentChunk.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(ContentChunk.class)));
            inSequence(consumerSequence);
            oneOf(consumer).consume(with(any(ContentChunk.class)));
            inSequence(consumerSequence);
            oneOf(consumer).endChunks();
            inSequence(consumerSequence);
            oneOf(consumer).endNode(with(any(Node.class)));
            inSequence(consumerSequence);
            oneOf(consumer).endRevision(with(any(Revision.class)));
            inSequence(consumerSequence);
            oneOf(consumer).finish();
            inSequence(consumerSequence);
        }});

        parser.Start(consumer);

        // actually record the FileContentChunks this time
        bais = new ByteArrayInputStream(baos.toByteArray());
        parser = new SvnDumpParser(new SvnDumpCharStream(bais));
        parser.setFileContentChunkSize(fileContentChunkSize);
        RepositoryInMemory inMemoryDump = new RepositoryInMemory();
        parser.Start(inMemoryDump);

        List<ContentChunk> chunks = inMemoryDump.getRepo().getRevisions().get(1).getNodes().get(0).getContent();
        assertThat(chunks.size(), is(3));
        assertThat(chunks.get(0).getContent().length, is(100));
        assertThat(chunks.get(1).getContent().length, is(100));
        assertThat(chunks.get(2).getContent().length, is(56));
    }

    @Test
    public void parse_binary_files() throws NoSuchAlgorithmException, ParseException {
        RepositoryImpl dump = new RepositoryImpl();
        byte[] content;
        {
            dump.setPreamble(new PreambleImpl("d3449ea3-e53b-4243-ab5a-b67b5a26103a"));

            Revision r0 = new RevisionImpl(0);

            content = new byte[1024];

            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte currentByte = (byte) -128;
            for (int i = 0; i < content.length; i++) {
                currentByte++;
                content[i] = currentByte;
                md5.update(currentByte);
            }

            NodeImpl binaryFile1 = new NodeImpl(r0);
            binaryFile1.getHeaders().put(NodeHeader.ACTION, "add");
            binaryFile1.getHeaders().put(NodeHeader.KIND, "file");
            binaryFile1.getHeaders().put(NodeHeader.PATH, "binaryFile1");
            binaryFile1.getHeaders().put(NodeHeader.TEXT_CONTENT_LENGTH, String.valueOf(content.length));
            binaryFile1.getHeaders().put(NodeHeader.MD5, TestUtil.md5ConvertDigest(md5.digest()));
            binaryFile1.addFileContentChunk(new ContentChunkImpl(content));

            r0.addNode(binaryFile1);
            dump.addRevision(r0);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RepositoryWriter writer = new SvnDumpWriter();
        writer.writeTo(baos);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        RepositoryInMemory inMemory = new RepositoryInMemory();

        SvnDumpParser.consume(new ByteArrayInputStream(baos.toByteArray()), inMemory);

        Repository recreatedDump = inMemory.getRepo();
        assertThat(recreatedDump.getRevisions().size(), is(1));

        Revision r0 = recreatedDump.getRevisions().get(0);
        assertThat(r0.getNodes().size(), is(1));

        Node n0_0 = r0.getNodes().get(0);
        assertThat(n0_0.getByteContent().length, is(equalTo(content.length)));

        assertThat(n0_0.getContent().size(), is(1));
        assertThat(n0_0.getContent().get(0).getContent(), is(equalTo(content)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void only_allow_SvnDumpFileCharStream() throws ParseException {
        Mockery mockery = new Mockery();
        CharStream fakeStream = mockery.mock(CharStream.class);
        RepositoryConsumer consumer = mockery.mock(RepositoryConsumer.class);
        new SvnDumpParser(fakeStream).Start(consumer);
    }

    @Test
    @Ignore
    public void parse_this_file() throws IOException, ParseException {
        ProcessBuilder processBuilder = new ProcessBuilder("/bin/cat", "/home/cosmin/Zoo/svndumpgui/DUMP");
        Process process = processBuilder.start();
        RepositoryWriter writer = new RepositorySummary();
        OutputStream os = new FileOutputStream(new File("/home/cosmin/Zoo/svndumpgui/runs/" + generateFileName()));
        writer.writeTo(os);

        SvnDumpCharStream charStream = new SvnDumpCharStream(process.getInputStream());
        PrintStream debugStream = new PrintStream(os);

        try {
            new SvnDumpParser(charStream).Start(writer);
        } catch(ParseException ex) {
            debugStream.println(ex.getMessage());
            fail(ex.getMessage());
        }
        System.out.flush();
    }

    private String generateFileName() {
        return new SimpleDateFormat("yyyyMMddhhmmss'.log'").format(new Date());
    }

    @Test(expected = ParseException.class)
    public void throw_exception_on_missing_nl() throws ParseException {
        Mockery context = new Mockery();
        Sequence consumerSequence = context.sequence("consumerSequence");
        RepositoryConsumer mockConsumer = context.mock(RepositoryConsumer.class);
        context.checking(new Expectations() {{
            oneOf(mockConsumer).consume(with(any(Preamble.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).consume(with(any(Revision.class))); inSequence(consumerSequence);
            oneOf(mockConsumer).endRevision(with(any(Revision.class))); inSequence(consumerSequence);
        }});

        SvnDumpParser.consume(TestUtil.openResource("dumps/invalid/missing_nl.invalid"), mockConsumer);
    }
}
