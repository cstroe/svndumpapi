package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Repository;
import com.github.cstroe.svndumpgui.api.RepositoryMutator;
import com.github.cstroe.svndumpgui.api.RepositoryWriter;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryInMemory;
import com.github.cstroe.svndumpgui.internal.writer.RepositoryWriterImpl;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static com.github.cstroe.svndumpgui.internal.utility.TestUtil.assertEqualStreams;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PathChangeTest {

    @Test
    public void file_name_change() throws Exception {
        final String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            RepositoryInMemory dumpInMemory = new RepositoryInMemory();
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), dumpInMemory);
            Repository dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("README.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("README.txt")));
        }
        {
            RepositoryMutator pathChange = new PathChange("README.txt", "README-changed.txt");
            RepositoryInMemory dumpInMemory = new RepositoryInMemory();
            pathChange.continueTo(dumpInMemory);
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), pathChange);
            Repository dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("README-changed.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("README-changed.txt")));
        }
    }

    @Test
    public void file_name_change_of_copy_path() throws Exception {
        String dumpFilePath = "dumps/svn_copy_file.dump";
        {
            RepositoryInMemory dumpInMemory = new RepositoryInMemory();
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), dumpInMemory);
            Repository dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("README.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(NodeHeader.COPY_FROM_PATH),
                    is(equalTo("README.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("OTHER.txt")));
        }
        {
            RepositoryMutator pathChange = new PathChange("README.txt", "README-changed.txt");
            RepositoryInMemory dumpInMemory = new RepositoryInMemory();
            pathChange.continueTo(dumpInMemory);
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), pathChange);
            Repository dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("README-changed.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(NodeHeader.COPY_FROM_PATH),
                    is(equalTo("README-changed.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(NodeHeader.PATH),
                    is(equalTo("OTHER.txt")));
        }
    }

    @Test
    public void update_mergeinfo() throws Exception {
        RepositoryMutator pathChange = new PathChange("branches", "custom_branches");
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        pathChange.continueTo(dumpInMemory);
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/simple_branch_and_merge.dump"), pathChange);
        Repository dump = dumpInMemory.getDump();

        ByteArrayOutputStream changedDump = new ByteArrayOutputStream();
        RepositoryWriter writer = new RepositoryWriterImpl();
        writer.writeTo(changedDump);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/simple_branch_and_merge_renamed.dump");

        ByteArrayInputStream bis = new ByteArrayInputStream(changedDump.toByteArray());

        assertEqualStreams(s, bis);
    }

    @Test
    public void update_multiline_mergeinfo() throws Exception {
        RepositoryMutator pathChange = new PathChange("branches/branch2", "branches/newbranchname");
        RepositoryInMemory dumpInMemory = new RepositoryInMemory();
        pathChange.continueTo(dumpInMemory);
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/many_branches.dump"), pathChange);
        Repository dump = dumpInMemory.getDump();

        ByteArrayOutputStream changedDump = new ByteArrayOutputStream();
        RepositoryWriter writer = new RepositoryWriterImpl();
        writer.writeTo(changedDump);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("dumps/many_branches_renamed.dump");

        ByteArrayInputStream bis = new ByteArrayInputStream(changedDump.toByteArray());

        assertEqualStreams(s, bis);
    }
}