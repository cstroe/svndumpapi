package com.github.cstroe.svndumpgui.internal.utility.range;

public class SpanImpl implements Span {
    private final int low;
    private final int high;

    public SpanImpl(int low, int high) {
        if((low == NEGATIVE_INFINITY && high == NEGATIVE_INFINITY) ||
           (low == POSITIVE_INFINITY && high == POSITIVE_INFINITY)) {
           throw new IllegalArgumentException("the same infinity cannot be specified for both low and high");
        }
        if(low > high) {
            throw new IllegalArgumentException("low is greater than high");
        }
        this.low = low;
        this.high = high;
    }

    @Override
    public int low() {
        return low;
    }

    @Override
    public int high() {
        return high;
    }
}
