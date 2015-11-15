package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.utility.Md5;
import com.github.cstroe.svndumpgui.internal.utility.Sha1;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriter;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FileContentReplaceTest {
    @Test
    public void simple_replace() throws ParseException, NoSuchAlgorithmException {
        final String newFileContent = "No content.\n";

        Predicate<Node> predicate = n -> n.getRevision().get().getNumber() == 1 && n.getHeaders().get(NodeHeader.PATH).equals("README.txt");
        FileContentReplace fileContentReplace = new FileContentReplace(predicate, n -> new ContentChunkImpl(newFileContent.getBytes()));

        RepositoryInMemory inMemory = new RepositoryInMemory();
        fileContentReplace.continueTo(inMemory);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);

        assertThat(inMemory.getRepo().getRevisions().size(), is(2));
        Revision r1 = inMemory.getRepo().getRevisions().get(1);
        assertThat(r1.getNodes().size(), is(1));
        Node node = r1.getNodes().get(0);
        assertThat(new String(node.getContent().get(0).getContent()), is(equalTo(newFileContent)));
        assertThat(node.get(NodeHeader.TEXT_CONTENT_LENGTH), is(equalTo("12")));
        assertThat(node.get(NodeHeader.CONTENT_LENGTH), is(equalTo("22")));
        assertThat(node.get(NodeHeader.MD5), is(equalTo(new Md5().hash(newFileContent.getBytes()))));
        assertThat(node.get(NodeHeader.SHA1), is(equalTo(new Sha1().hash(newFileContent.getBytes()))));
    }

    @Test
    public void if_no_match_then_no_change() throws ParseException, IOException {
        Predicate<Node> predicate = n -> false;
        FileContentReplace fileContentReplace = new FileContentReplace(predicate, n -> null);

        ByteArrayOutputStream newDump = new ByteArrayOutputStream();
        RepositoryWriter svnDumpWriter = new SvnDumpWriter();
        svnDumpWriter.writeTo(newDump);

        fileContentReplace.continueTo(svnDumpWriter);

        SvnDumpParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);

        TestUtil.assertEqualStreams(TestUtil.openResource("dumps/add_file.dump"), new ByteArrayInputStream(newDump.toByteArray()));
    }
}