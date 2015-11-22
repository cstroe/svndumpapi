package com.github.cstroe.svndumpgui.internal.utility.range;

public class SpanImpl implements Span {
    private int low;
    private int high;

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

    @Override
    public void low(int value) {
        this.low = value;
    }

    @Override
    public void high(int value) {
        this.high = value;
    }

    @Override
    public String toString() {
        return low + "-" + high;
    }
}
