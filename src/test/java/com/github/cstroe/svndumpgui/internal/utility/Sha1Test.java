package com.github.cstroe.svndumpgui.internal.utility;

import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class Sha1Test {
    @Test
    public void correct_sum() {
        assertThat(new Sha1().hash("1234\n".getBytes()), is(equalTo("1be168ff837f043bde17c0314341c84271047b31")));
    }
}
