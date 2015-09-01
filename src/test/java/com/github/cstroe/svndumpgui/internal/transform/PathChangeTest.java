package com.github.cstroe.svndumpgui.internal.transform;

import com.github.cstroe.svndumpgui.api.SvnDump;
import com.github.cstroe.svndumpgui.api.SvnDumpMutator;
import com.github.cstroe.svndumpgui.api.SvnDumpWriter;
import com.github.cstroe.svndumpgui.api.SvnNodeHeader;
import com.github.cstroe.svndumpgui.internal.SvnDumpFileParserTest;
import com.github.cstroe.svndumpgui.internal.SvnDumpWriterImpl;
import com.github.cstroe.svndumpgui.internal.SvnDumpWriterImplTest;
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
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_multi_file_delete.dump");

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH),
                is(equalTo("README.txt")));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.PATH),
                is(equalTo("README.txt")));

        SvnDumpMutator pathChange = new PathChange("README.txt", "README-changed.txt");
        pathChange.mutate(dump);

        assertThat(dump.getRevisions().size(), is(3));
        assertThat(dump.getRevisions().get(0).getNodes().size(), is(0));
        assertThat(dump.getRevisions().get(1).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(1).getNodes().get(0).get(SvnNodeHeader.PATH),
                is(equalTo("README-changed.txt")));
        assertThat(dump.getRevisions().get(2).getNodes().size(), is(3));
        assertThat(dump.getRevisions().get(2).getNodes().get(0).get(SvnNodeHeader.PATH),
                is(equalTo("README-changed.txt")));

    }

    @Test
    public void file_name_change_of_copy_path() throws Exception {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/svn_copy_file.dump");

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

        SvnDumpMutator pathChange = new PathChange("README.txt", "README-changed.txt");
        pathChange.mutate(dump);

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

    @Test
    public void update_mergeinfo() throws Exception {
        SvnDump dump = SvnDumpFileParserTest.parse("dumps/simple_branch_and_merge.dump");

        SvnDumpMutator pathChange = new PathChange("branches", "custom_branches");
        pathChange.mutate(dump);

        ByteArrayOutputStream changedDump = new ByteArrayOutputStream();
        SvnDumpWriter writer = new SvnDumpWriterImpl();
        writer.write(changedDump, dump);

        final InputStream s = Thread.currentThread().getContextClassLoader()
            .getResourceAsStream("dumps/simple_branch_and_merge_renamed.dump");

        ByteArrayInputStream bis = new ByteArrayInputStream(changedDump.toByteArray());

        SvnDumpWriterImplTest.assertEqualStreams(s, bis);
    }
}