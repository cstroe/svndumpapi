package com.github.cstroe.svndumpgui.internal.transform.property;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class MergeInfoDataTest {
    @Test
    public void test_path_toString() {
        {
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(1234));
            assertThat(path.toString(), is(equalTo("/some/path:1234\n")));
        }{
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(1234, 2345));
            assertThat(path.toString(), is(equalTo("/some/path:1234-2345\n")));
        }{
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(1234));
            path.addRange(new MergeInfoData.Range(2000, 2345));
            assertThat(path.toString(), is(equalTo("/some/path:1234,2000-2345\n")));
        }{
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(2000, 2345));
            path.addRange(new MergeInfoData.Range(3345));
            assertThat(path.toString(), is(equalTo("/some/path:2000-2345,3345\n")));
        }{
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(2000, 2345));
            path.addRange(new MergeInfoData.Range(3345, 3346));
            assertThat(path.toString(), is(equalTo("/some/path:2000-2345,3345-3346\n")));
        }
    }

    @Test
    public void test_range_toString() {
        {
            MergeInfoData.Range range = new MergeInfoData.Range(1234);
            assertThat(range.toString(), is(equalTo("1234")));
        }{
            MergeInfoData.Range range = new MergeInfoData.Range(1234, 2345);
            assertThat(range.toString(), is(equalTo("1234-2345")));
        }
    }

    @Test
    public void test_toString_with_empty_data() {
        assertThat(new MergeInfoData().toString(), is(equalTo("")));
    }

    @Test
    public void test_toString_with_one_path() {
        {
            MergeInfoData data = new MergeInfoData();
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(1234));
            data.addPath(path);
            assertThat(data.toString(), is(equalTo("/some/path:1234\n")));
        }{
            MergeInfoData data = new MergeInfoData();
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(1234,2345));
            data.addPath(path);
            assertThat(data.toString(), is(equalTo("/some/path:1234-2345\n")));
        }{
            MergeInfoData data = new MergeInfoData();
            MergeInfoData.Path path = new MergeInfoData.Path("/some/path");
            path.addRange(new MergeInfoData.Range(1234));
            path.addRange(new MergeInfoData.Range(1236,2345));
            data.addPath(path);
            assertThat(data.toString(), is(equalTo("/some/path:1234,1236-2345\n")));
        }
    }
}