package com.github.cstroe.svndumpgui.internal.transform.property;

import com.github.cstroe.svndumpgui.generated.MergeInfoParser;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.TokenMgrError;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MergeInfoParserTest {
    @Test
    public void parse_empty_string() throws ParseException {
        MergeInfoData data = MergeInfoParser.parse("");

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(0));
    }

    @Test
    public void parse_one_line_single_range_with_one_revision() throws ParseException {
        String mergeInfo = "/some/path/here:123456\n";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(1));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(range.isNonInheritable());
    }

    @Test
    public void parse_one_line_single_range() throws ParseException {
        String mergeInfo = "/some/path/here:123456-234567\n";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(1));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(234567));
        assertFalse(range.isNonInheritable());
    }

    @Test
    public void parse_one_line_two_ranges() throws ParseException {
        String mergeInfo = "/some/path/here:33,123456-234567\n";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(2));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(33));
        assertThat(range.getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(range.isNonInheritable());

        range = path.getRanges().get(1);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(234567));
        assertFalse(range.isNonInheritable());
    }

    @Test
    public void parse_one_line_two_ranges_no_trailing_NL() throws ParseException {
        String mergeInfo = "/some/path/here:33,123456-234567";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertNotNull(data);
        assertThat(data.getPaths().size(), is(1));

        MergeInfoData.Path path = data.getPaths().get(0);
        assertThat(path.getPath(), is(equalTo("/some/path/here")));
        assertThat(path.getRanges().size(), is(2));

        MergeInfoData.Range range = path.getRanges().get(0);
        assertThat(range.getFromRange(), is(33));
        assertThat(range.getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(range.isNonInheritable());

        range = path.getRanges().get(1);
        assertThat(range.getFromRange(), is(123456));
        assertThat(range.getToRange(), is(234567));
        assertFalse(range.isNonInheritable());
    }

    @Test
    public void two_paths_with_trailing_NL() throws ParseException {
        String mergeInfo = "/some/path/here:33,123456-234567\n/some/path/here:12345-234567,34\n";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertThat(data.getPaths().size(), is(2));

        assertThat(data.getPaths().get(0).getRanges().size(), is(2));
        assertThat(data.getPaths().get(1).getRanges().size(), is(2));

        assertThat(data.getPaths().get(0).getRanges().get(0).getFromRange(), is(33));
        assertThat(data.getPaths().get(0).getRanges().get(0).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(data.getPaths().get(0).getRanges().get(0).isNonInheritable());
        assertThat(data.getPaths().get(0).getRanges().get(1).getFromRange(), is(123456));
        assertThat(data.getPaths().get(0).getRanges().get(1).getToRange(), is(234567));
        assertFalse(data.getPaths().get(0).getRanges().get(1).isNonInheritable());

        assertThat(data.getPaths().get(1).getRanges().get(0).getFromRange(), is(12345));
        assertThat(data.getPaths().get(1).getRanges().get(0).getToRange(), is(234567));
        assertFalse(data.getPaths().get(1).getRanges().get(0).isNonInheritable());
        assertThat(data.getPaths().get(1).getRanges().get(1).getFromRange(), is(34));
        assertThat(data.getPaths().get(1).getRanges().get(1).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(data.getPaths().get(1).getRanges().get(1).isNonInheritable());
    }

    @Test
    public void two_paths_no_trailing_NL() throws ParseException {
        String mergeInfo = "/some/path/here:33,123456-234567\n/some/path/here:12345-234567,34";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertThat(data.getPaths().size(), is(2));

        assertThat(data.getPaths().get(0).getRanges().size(), is(2));
        assertThat(data.getPaths().get(1).getRanges().size(), is(2));

        assertThat(data.getPaths().get(0).getRanges().get(0).getFromRange(), is(33));
        assertThat(data.getPaths().get(0).getRanges().get(0).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(data.getPaths().get(0).getRanges().get(0).isNonInheritable());
        assertThat(data.getPaths().get(0).getRanges().get(1).getFromRange(), is(123456));
        assertThat(data.getPaths().get(0).getRanges().get(1).getToRange(), is(234567));
        assertFalse(data.getPaths().get(0).getRanges().get(1).isNonInheritable());

        assertThat(data.getPaths().get(1).getRanges().get(0).getFromRange(), is(12345));
        assertThat(data.getPaths().get(1).getRanges().get(0).getToRange(), is(234567));
        assertFalse(data.getPaths().get(1).getRanges().get(0).isNonInheritable());
        assertThat(data.getPaths().get(1).getRanges().get(1).getFromRange(), is(34));
        assertThat(data.getPaths().get(1).getRanges().get(1).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertFalse(data.getPaths().get(1).getRanges().get(1).isNonInheritable());
    }

    @Test(expected = TokenMgrError.class)
    public void missing_newline() throws ParseException {
        String mergeInfo = "/some/path/here:33,123456-234567/some/path/here:33,123456-234567";
        MergeInfoParser.parse(mergeInfo);
    }

    @Test
    public void non_inheritable_merge_info() throws ParseException {
        String mergeInfo = "/trunk/file:1234*,1255*,1265*,1266*,1267-1357*,1359*,2001*";
        MergeInfoData data = MergeInfoParser.parse(mergeInfo);

        assertThat(data.getPaths().size(), is(1));
        assertThat(data.getPaths().get(0).getRanges().size(), is(7));

        assertThat(data.getPaths().get(0).getRanges().get(0).getFromRange(), is(1234));
        assertThat(data.getPaths().get(0).getRanges().get(0).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertTrue(data.getPaths().get(0).getRanges().get(0).isNonInheritable());

        assertThat(data.getPaths().get(0).getRanges().get(1).getFromRange(), is(1255));
        assertThat(data.getPaths().get(0).getRanges().get(1).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertTrue(data.getPaths().get(0).getRanges().get(1).isNonInheritable());

        assertThat(data.getPaths().get(0).getRanges().get(2).getFromRange(), is(1265));
        assertThat(data.getPaths().get(0).getRanges().get(2).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertTrue(data.getPaths().get(0).getRanges().get(2).isNonInheritable());

        assertThat(data.getPaths().get(0).getRanges().get(3).getFromRange(), is(1266));
        assertThat(data.getPaths().get(0).getRanges().get(3).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertTrue(data.getPaths().get(0).getRanges().get(3).isNonInheritable());

        assertThat(data.getPaths().get(0).getRanges().get(4).getFromRange(), is(1267));
        assertThat(data.getPaths().get(0).getRanges().get(4).getToRange(), is(1357));
        assertTrue(data.getPaths().get(0).getRanges().get(4).isNonInheritable());

        assertThat(data.getPaths().get(0).getRanges().get(5).getFromRange(), is(1359));
        assertThat(data.getPaths().get(0).getRanges().get(5).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertTrue(data.getPaths().get(0).getRanges().get(5).isNonInheritable());

        assertThat(data.getPaths().get(0).getRanges().get(6).getFromRange(), is(2001));
        assertThat(data.getPaths().get(0).getRanges().get(6).getToRange(), is(MergeInfoData.Range.NOT_SET));
        assertTrue(data.getPaths().get(0).getRanges().get(6).isNonInheritable());
    }
}
