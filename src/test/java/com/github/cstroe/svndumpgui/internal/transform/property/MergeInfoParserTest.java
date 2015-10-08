package com.github.cstroe.svndumpgui.internal.transform.property;

import com.github.cstroe.svndumpgui.generated.MergeInfoParser;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpFileCharStream;
import org.junit.Test;

import java.io.ByteArrayInputStream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;

public class MergeInfoParserTest {
    private MergeInfoData parseMergeInfo(String mergeInfo) throws ParseException {
        MergeInfoParser parser = new MergeInfoParser(new SvnDumpFileCharStream(new ByteArrayInputStream(mergeInfo.getBytes())));
         return parser.Start();
    }

    @Test
    public void parse_one_line_single_range_with_one_revision() throws ParseException {
        String mergeInfo = "/some/path/here:123456\n";
        MergeInfoData data = parseMergeInfo(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(1));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(MergeInfoData.Range.NOT_SET));
    }

    @Test
    public void parse_one_line_single_range() throws ParseException {
        String mergeInfo = "/some/path/here:123456-234567\n";
        MergeInfoData data = parseMergeInfo(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(1));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(234567));
    }

    @Test
    public void parse_one_line_two_ranges() throws ParseException {
        String mergeInfo = "/some/path/here:33,123456-234567\n";
        MergeInfoData data = parseMergeInfo(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(2));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(33));
        assertThat(range.getToRange(), is(MergeInfoData.Range.NOT_SET));

        range = path.getRanges().get(1);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(234567));
    }
}
