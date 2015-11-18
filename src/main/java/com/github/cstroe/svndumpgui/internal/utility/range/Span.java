package com.github.cstroe.svndumpgui.internal.utility.range;

public interface Span {
    static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    static final int POSITIVE_INFINITY = Integer.MAX_VALUE;
    int low();
    int high();
    boolean contains(int value);
}
