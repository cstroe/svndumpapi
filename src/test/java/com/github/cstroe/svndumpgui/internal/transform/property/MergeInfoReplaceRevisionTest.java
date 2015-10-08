package com.github.cstroe.svndumpgui.internal.transform.property;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class MergeInfoReplaceRevisionTest {

    private void test(MergeInfoReplaceRevision replaceRevision, String before, String after) {
        assertThat(replaceRevision.apply(before), is(equalTo(after)));
    }

    @Test
    public void replace_revision_in_single_line() {
        MergeInfoReplaceRevision replaceRevision = new MergeInfoReplaceRevision(0, 1);
        {
            final String before = "/branches/b1:0-9999\n";
            final String after = "/branches/b1:1-9999\n";

            test(replaceRevision, before, after);
        }
        {
            final String before = "/branches/b1:0-9999,11000-12000\n";
            final String after = "/branches/b1:1-9999,11000-12000\n";

            test(replaceRevision, before, after);
        }
    }

    @Test
    public void replace_revision_in_multiple_lines() {
        MergeInfoReplaceRevision replaceRevision = new MergeInfoReplaceRevision(0, 1);

        {
            final String before = "/branches/b1:0-9999\n";
            final String after = "/branches/b1:1-9999\n";

            test(replaceRevision, before + before + before + before,
                    after + after + after + after);
        }{
            final String before = "/branches/b1:0-9999,11000-12000\n";
            final String after = "/branches/b1:1-9999,11000-12000\n";

            test(replaceRevision, before + before + before + before,
                    after + after + after + after);
        }
    }
}