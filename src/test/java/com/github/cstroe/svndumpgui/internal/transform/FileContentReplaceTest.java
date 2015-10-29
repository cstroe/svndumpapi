package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.FileContentChunk;
import com.github.cstroe.svndumpgui.api.SvnNode;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.api.SvnRevision;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.FileContentChunkImpl;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpInMemory;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class FileContentReplaceTest {
    @Test
    public void simple_replace() throws ParseException, NoSuchAlgorithmException {
        final String newFileContent = "No content.\n";

        Predicate<SvnNode> predicate = n -> n.getRevision().get().getNumber() == 1 && n.getHeaders().get(SvnNodeHeader.PATH).equals("README.txt");
        List<FileContentChunk> contentList = new LinkedList<>();
        contentList.add(new FileContentChunkImpl(newFileContent.getBytes()));
        FileContentReplace fileContentReplace = new FileContentReplace(predicate, contentList);

        SvnDumpInMemory inMemory = new SvnDumpInMemory();
        fileContentReplace.continueTo(inMemory);

        SvnDumpFileParser.consume(TestUtil.openResource("dumps/add_file.dump"), fileContentReplace);

        assertThat(inMemory.getDump().getRevisions().size(), is(2));
        SvnRevision r1 = inMemory.getDump().getRevisions().get(1);
        assertThat(r1.getNodes().size(), is(1));
        SvnNode node = r1.getNodes().get(0);
        assertThat(new String(node.getContent().get(0).getContent()), is(equalTo(newFileContent)));
        assertThat(node.get(SvnNodeHeader.MD5), is(equalTo(TestUtil.md5sum(newFileContent.getBytes()))));
    }
}