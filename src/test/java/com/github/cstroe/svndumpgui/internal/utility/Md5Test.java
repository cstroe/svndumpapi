package com.github.cstroe.svndumpgui.internal.utility;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Md5Test {
    @Test
    public void correct_sum() {
        assertThat(new Md5().hash("1234\n".getBytes()), is(equalTo("e7df7cd2ca07f4f1ab415d457a6e1c13")));
    }
}