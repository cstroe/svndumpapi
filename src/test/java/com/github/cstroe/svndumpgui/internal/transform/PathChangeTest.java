package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImpl;
import com.github.cstroe.svndumpgui.internal.writer.SvnDumpWriterImplTest;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileParserDoppelganger;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class PathChangeTest {

    @Test
    public void file_name_change() throws Exception {
        final String dumpFilePath = "dumps/svn_multi_file_delete.dump";
        {
            SvnDump dump = SvnDumpFileParserDoppelganger.parse(dumpFilePath);

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
            SvnDump dump = SvnDumpFileParserDoppelganger.consume(dumpFilePath, pathChange);

            assertThat(dump.getRevisions().size(), is(3));
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
            SvnDump dump = SvnDumpFileParserDoppelganger.parse(dumpFilePath);

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
            SvnDump dump = SvnDumpFileParserDoppelganger.consume(dumpFilePath, pathChange);

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
        SvnDump dump = SvnDumpFileParserDoppelganger.consume("dumps/simple_branch_and_merge.dump", pathChange);

        ByteArrayOutputStream changedDump = new ByteArrayOutputStream();
        SvnDumpWriter writer = new SvnDumpWriterImpl();
        writer.writeTo(changedDump);
        SvnDumpFileParserDoppelganger.consume(dump, writer);

        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/simple_branch_and_merge_renamed.dump");

        ByteArrayInputStream bis = new ByteArrayInputStream(changedDump.toByteArray());

        SvnDumpWriterImplTest.assertEqualStreams(s, bis);
    }
}