package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.generated.SvnDumpFileParser;
import com.github.cstroe.svndumpgui.internal.utility.TestUtil;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpInMemory;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImpl;
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
            SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), dumpInMemory);
            SvnDump dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("README.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("README.txt")));
        }
        {
            SvnDumpMutator pathChange = new PathChange("README.txt", "README-changed.txt");
            SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
            pathChange.continueTo(dumpInMemory);
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), pathChange);
            SvnDump dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("README-changed.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("README-changed.txt")));
        }
    }

    @Test
    public void file_name_change_of_copy_path() throws Exception {
        String dumpFilePath = "dumps/svn_copy_file.dump";
        {
            SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), dumpInMemory);
            SvnDump dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("README.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.COPY_FROM_PATH),
                    is(equalTo("README.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("OTHER.txt")));
        }
        {
            SvnDumpMutator pathChange = new PathChange("README.txt", "README-changed.txt");
            SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
            pathChange.continueTo(dumpInMemory);
            SvnDumpFileParser.consume(TestUtil.openResource(dumpFilePath), pathChange);
            SvnDump dump = dumpInMemory.getDump();

            assertThat(dump.getRevisions().size(), is(3));
            assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
            assertThat(dump.getRevisions().get(1).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("README-changed.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().size(), is(1));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.COPY_FROM_PATH),
                    is(equalTo("README-changed.txt")));
            assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.PATH),
                    is(equalTo("OTHER.txt")));
        }
    }

    @Test
    public void update_mergeinfo() throws Exception {
        SvnDumpMutator pathChange = new PathChange("branches", "custom_branches");
        SvnDumpInMemory dumpInMemory = new SvnDumpInMemory();
        pathChange.continueTo(dumpInMemory);
        SvnDumpFileParser.consume(TestUtil.openResource("dumps/simple_branch_and_merge.dump"), pathChange);
        SvnDump dump = dumpInMemory.getDump();

        ByteArrayOutputStream changedDump = new ByteArrayOutputStream();
        SvnDumpWriter writer = new SvnDumpWriterImpl();
        writer.writeTo(changedDump);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/simple_branch_and_merge_renamed.dump");

        ByteArrayInputStream bis = new ByteArrayInputStream(changedDump.toByteArray());

        assertEqualStreams(s, bis);
    }
}