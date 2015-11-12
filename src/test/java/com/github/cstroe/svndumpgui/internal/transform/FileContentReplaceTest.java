package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.ContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import org.junit.Test;

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

        SvnDumpFileParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);

        assertThat(inMemory.getDump().getRevisions().size(), is(2));
        Revision r1 = inMemory.getDump().getRevisions().get(1);
        assertThat(r1.getNodes().size(), is(1));
        Node node = r1.getNodes().get(0);
        assertThat(new String(node.getContent().get(0).getContent()), is(equalTo(newFileContent)));
        assertThat(node.get(NodeHeader.MD5), is(equalTo(TestUtil.md5sum(newFileContent.getBytes()))));
    }
}