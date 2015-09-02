package com.github.cstroe.svndumpgui.internal;

import com.github.cstroe.svndumpgui.api.*;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpInMemory;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class SvnDumpFileParserTest {

    public static SvnDump parse(String dumpFile) throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream(dumpFile);
        return parse(s);
    }

    public static SvnDump parse(InputStream is) throws ParseException {
        SvnDumpFileParser parser = new SvnDumpFileParser(is, "ISO-8859-1");
        SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
        parser.Start(dumpInMemory);
        return dumpInMemory.getDump();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void simple_property_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/simple_property.fragment");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);

        SvnRevisionImpl revision = new SvnRevisionImpl(0);
        Map properties = parser.Property();
        revision.setProperties(properties);

        assertNotNull(revision.get(SvnProperty.DATE));
        assertThat(revision.get(SvnProperty.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void empty_revision_is_parsed() throws ParseException {
        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/partial/empty_revision.fragment");

        SvnDumpFileParser parser = new SvnDumpFileParser(s);
        SvnRevision revision = parser.Revision();

        assertNotNull(revision);
        assertThat(revision.getNumber(), is(0));
        assertThat(revision.get(SvnProperty.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void should_parse_uuid() throws IOException, ParseException {
        SvnDump dump = parse("dumps/empty.dump");

        assertNotNull(dump);
        assertThat(dump.getPreamble().getUUID(), is(equalTo("0c9743f5-f757-4bed-a5b3-acbcba4d645b")));
    }

    @Test
    public void should_parse_empty_dump() throws IOException, ParseException {
        SvnDump dump = parse("dumps/empty.dump");

        assertNotNull(dump);

        List<SvnRevision> revisionList = dump.getRevisions();

        assertThat("There should be a revision present", revisionList.size(), is(1));

        SvnRevision firstRevision = revisionList.get(0);

        assertThat(firstRevision.getNumber(), is(0));
        assertNotNull(firstRevision.get(SvnProperty.DATE));
        assertThat(firstRevision.get(SvnProperty.DATE), is(equalTo("2015-08-07T13:52:20.465543Z")));
    }

    @Test
    public void should_parse_one_commit() throws ParseException {
        SvnDump dump = parse("dumps/firstcommit.dump");

        assertThat("The repository dump contains two revisions.", dump.getRevisions().size(), is(2));

        assertThat(dump.getRevisions().get(0).getNumber(), is(0));
        assertThat(dump.getRevisions().get(0).get(SvnProperty.DATE), is(equalTo("2015-08-27T02:50:19.465543Z")));

        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).get(SvnProperty.DATE), is(equalTo("2015-08-27T05:38:16.553074Z")));
        assertThat(dump.getRevisions().get(1).get(SvnProperty.AUTHOR), is(equalTo("cosmin")));
        assertThat(dump.getRevisions().get(1).get(SvnProperty.LOG), is(equalTo("Added a first file.")));

        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        SvnNode fileNode = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(fileNode.get(SvnNodeHeader.PATH), is(equalTo("firstFile.txt")));
        assertThat(fileNode.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(fileNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(fileNode.get(SvnNodeHeader.MD5), is(equalTo("d41d8cd98f00b204e9800998ecf8427e")));
        assertThat(fileNode.get(SvnNodeHeader.SHA1), is(equalTo("da39a3ee5e6b4b0d3255bfef95601890afd80709")));
    }

    @Test
    public void should_parse_file_content() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/add_file.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        SvnNode readmeTxt = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(readmeTxt.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(readmeTxt.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(readmeTxt.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(readmeTxt.get(SvnNodeHeader.MD5), is(equalTo("4221d002ceb5d3c9e9137e495ceaa647")));
        assertThat(readmeTxt.get(SvnNodeHeader.SHA1), is(equalTo("804d716fc5844f1cc5516c8f0be7a480517fdea2")));
        assertThat(new String(readmeTxt.getContent()), is(equalTo("this is a test file\n")));

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5raw = md5.digest(readmeTxt.getContent());
        String md5sum = md5sum(md5raw);
        assertThat(md5sum, is(equalTo(readmeTxt.get(SvnNodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(SvnNodeHeader.SHA1))));
    }

    @Test
    public void should_allow_for_optional_properties_on_nodes() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/add_file_no_node_properties.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        SvnNode readmeTxt = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(readmeTxt.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(readmeTxt.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(readmeTxt.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertThat(readmeTxt.get(SvnNodeHeader.MD5), is(equalTo("4221d002ceb5d3c9e9137e495ceaa647")));
        assertThat(readmeTxt.get(SvnNodeHeader.SHA1), is(equalTo("804d716fc5844f1cc5516c8f0be7a480517fdea2")));
        assertThat(new String(readmeTxt.getContent()), is(equalTo("this is a test file\n")));

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5raw = md5.digest(readmeTxt.getContent());
        String md5sum = md5sum(md5raw);
        assertThat(md5sum, is(equalTo(readmeTxt.get(SvnNodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(SvnNodeHeader.SHA1))));
    }

    private String md5sum(byte[] digest) {
        return toHex(digest, 32);
    }

    private String sha1sum(byte[] digest) {
        return toHex(digest, 40);
    }

    // swiped from http://stackoverflow.com/questions/415953
    private String toHex(byte[] digest, int length) {
        BigInteger bitInt = new BigInteger(1, digest);
        String hashText = bitInt.toString(16);

        while(hashText.length() < length) {
            hashText = "0" + hashText;
        }

        return hashText;
    }

    /**
     * We should be flexible with the order of the Node headers.
     *
     * For example, sometimes "Node-path" come before "Node-kind", other
     * times it's the reverse.  We should parse both correctly.
     */
    @Test
    public void should_parse_nodes_with_different_ordering_of_headers() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/different_node_order.dump");

        assertThat(dump.getRevisions().size(), is(2));

        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH), is("AM-Core"));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.KIND), is("dir"));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.ACTION), is("add"));
    }

    /**
     * The ordering of all the node headers should not matter.
     */
    @Test
    public void should_parse_nodes_with_different_ordering_of_headers2() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/different_node_order2.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        SvnNode svnNode = dump.getRevisions().get(1).getNodes().get(0);
        assertThat(svnNode.get(SvnNodeHeader.PATH), is("AM-Core"));
        assertThat(svnNode.get(SvnNodeHeader.KIND), is("dir"));
        assertThat(svnNode.get(SvnNodeHeader.ACTION), is("add"));
        assertThat(svnNode.get(SvnNodeHeader.SHA1), is("53ff16933cc0ec0077ea0d5f848ef0fd61440c27"));
    }

    @Test
    public void should_parse_binary_file() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/binary_commit.dump");

        assertThat(dump.getRevisions().size(), is(2));
        assertThat(dump.getRevisions().get(1).getNumber(), is(1));
        assertThat(dump.getRevisions().get(1).get(SvnProperty.LOG), is("Adding binary file."));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));

        SvnNode fileBin = dump.getRevisions().get(1).getNodes().get(0);

        assertThat(fileBin.getProperties().get(SvnProperty.MIMETYPE), is(equalTo("application/octet-stream")));

        assertThat(fileBin.getContent().length, is(1024));
        assertThat(fileBin.get(SvnNodeHeader.PATH), is("file.bin"));

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5raw = md5.digest(fileBin.getContent());
        String md5sum = md5sum(md5raw);
        assertThat(md5sum, is(equalTo(fileBin.get(SvnNodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(fileBin.getContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(fileBin.get(SvnNodeHeader.SHA1))));
    }

    @Test
    public void should_parse_svn_mv_operations() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/svn_rename.dump");

        assertThat(dump.getRevisions().size(), is(3));

        // validate the revision in which we create the file
        SvnRevision createFileRevision = dump.getRevisions().get(1);
        assertThat(createFileRevision.getNumber(), is(1));
        assertThat(createFileRevision.get(SvnProperty.LOG), is(equalTo("Committed README.txt")));
        assertThat(createFileRevision.getNodes().size(), is(1));

        SvnNode readmeTxt = createFileRevision.getNodes().get(0);

        assertThat(readmeTxt.getContent().length, is(20));
        assertThat(readmeTxt.get(SvnNodeHeader.PATH), is("README.txt"));

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5raw = md5.digest(readmeTxt.getContent());
        String md5sum = md5sum(md5raw);
        assertThat(md5sum, is(equalTo(readmeTxt.get(SvnNodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(SvnNodeHeader.SHA1))));

        // validate the revision in which we rename the file
        SvnRevision moveFileRevision = dump.getRevisions().get(2);
        assertThat(moveFileRevision.get(SvnProperty.LOG), is(equalTo("Renamed README.txt to README-new.txt")));
        assertThat(moveFileRevision.getNodes().size(), is(2));

        SvnNode newFileNode = moveFileRevision.getNodes().get(0);
        assertThat(newFileNode.get(SvnNodeHeader.PATH), is(equalTo("README-new.txt")));
        assertThat(newFileNode.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(newFileNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertNull(newFileNode.getContent());
        assertThat(newFileNode.get(SvnNodeHeader.COPY_FROM_REV), is(equalTo("1")));
        assertThat(newFileNode.get(SvnNodeHeader.COPY_FROM_PATH), is(equalTo("README.txt")));
        assertThat(newFileNode.get(SvnNodeHeader.SOURCE_MD5), is(equalTo(readmeTxt.get(SvnNodeHeader.MD5))));
        assertThat(newFileNode.get(SvnNodeHeader.SOURCE_SHA1), is(equalTo(readmeTxt.get(SvnNodeHeader.SHA1))));

        SvnNode oldFileNode  = moveFileRevision.getNodes().get(1);
        assertThat(oldFileNode.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(oldFileNode.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
    }

    @Test
    public void should_parse_svn_mv_operations_without_copy_hashes() throws ParseException, NoSuchAlgorithmException {
        SvnDump dump = parse("dumps/svn_rename_no_copy_hashes.dump");

        assertThat(dump.getRevisions().size(), is(3));

        // validate the revision in which we create the file
        SvnRevision createFileRevision = dump.getRevisions().get(1);
        assertThat(createFileRevision.getNumber(), is(1));
        assertThat(createFileRevision.get(SvnProperty.LOG), is(equalTo("Committed README.txt")));
        assertThat(createFileRevision.getNodes().size(), is(1));

        SvnNode readmeTxt = createFileRevision.getNodes().get(0);

        assertThat(readmeTxt.getContent().length, is(20));
        assertThat(readmeTxt.get(SvnNodeHeader.PATH), is("README.txt"));

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5raw = md5.digest(readmeTxt.getContent());
        String md5sum = md5sum(md5raw);
        assertThat(md5sum, is(equalTo(readmeTxt.get(SvnNodeHeader.MD5))));

        MessageDigest sha1 = MessageDigest.getInstance("SHA1");
        byte[] sha1raw = sha1.digest(readmeTxt.getContent());
        String sha1sum = sha1sum(sha1raw);
        assertThat(sha1sum, is(equalTo(readmeTxt.get(SvnNodeHeader.SHA1))));

        // validate the revision in which we rename the file
        SvnRevision moveFileRevision = dump.getRevisions().get(2);
        assertThat(moveFileRevision.get(SvnProperty.LOG), is(equalTo("Renamed README.txt to README-new.txt")));
        assertThat(moveFileRevision.getNodes().size(), is(2));

        SvnNode newFileNode = moveFileRevision.getNodes().get(0);
        assertThat(newFileNode.get(SvnNodeHeader.PATH), is(equalTo("README-new.txt")));
        assertThat(newFileNode.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(newFileNode.get(SvnNodeHeader.ACTION), is(equalTo("add")));
        assertNull(newFileNode.getContent());
        assertThat(newFileNode.get(SvnNodeHeader.COPY_FROM_REV), is(equalTo("1")));
        assertThat(newFileNode.get(SvnNodeHeader.COPY_FROM_PATH), is(equalTo("README.txt")));

        SvnNode oldFileNode  = moveFileRevision.getNodes().get(1);
        assertThat(oldFileNode.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(oldFileNode.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
    }

    @Test
    public void should_parse_directory() throws ParseException {
        SvnDump dump = parse("dumps/svn_add_directory.dump");

        assertThat(dump.getRevisions().size(), is(3));

        SvnRevision r1 = dump.getRevisions().get(1);

        assertThat(r1.getNumber(), is(1));
        assertThat(r1.getNodes().size(), is(1));

        SvnNode dir = r1.getNodes().get(0);
        assertThat(dir.get(SvnNodeHeader.KIND), is(equalTo("dir")));
        assertThat(dir.get(SvnNodeHeader.PATH), is(equalTo("testdir")));
        assertThat(dir.get(SvnNodeHeader.ACTION), is(equalTo("add")));

        SvnRevision r2 = dump.getRevisions().get(2);

        assertThat(r2.getNumber(), is(2));
        assertThat(r2.getNodes().size(), is(1));

        SvnNode file = r2.getNodes().get(0);
        assertThat(file.get(SvnNodeHeader.KIND), is(equalTo("file")));
        assertThat(file.get(SvnNodeHeader.PATH), is(equalTo("testdir/README.txt")));
        assertThat(file.get(SvnNodeHeader.ACTION), is(equalTo("add")));
    }

    @Test
    public void should_parse_file_deletion() throws ParseException {
        SvnDump dump = parse("dumps/svn_delete_file.dump");

        assertThat(dump.getRevisions().size(), is(4));

        SvnRevision r2 = dump.getRevisions().get(2);

        assertThat(r2.getNumber(), is(2));
        assertThat(r2.getNodes().size(), is(1));

        SvnNode fileDelete = r2.getNodes().get(0);
        assertNull(fileDelete.get(SvnNodeHeader.KIND));
        assertThat(fileDelete.get(SvnNodeHeader.PATH), is(equalTo("README.txt")));
        assertThat(fileDelete.get(SvnNodeHeader.ACTION), is(equalTo("delete")));
    }
}
