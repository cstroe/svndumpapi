package com.github.cstroe.svndumpgui.internal.utility.range;

public interface Span {
    static final int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    static final int POSITIVE_INFINITY = Integer.MAX_VALUE;
    int low();
    int high();

    default boolean contains(int value) {
        return value >= low() && value <= high();
    }

    @SuppressWarnings("RedundantIfStatement") // for readability
    default boolean overlaps(Span span) {
        // span is below the current span
        if(span.high() < low()) {
            return false;
        }
        // span is above the current span
        if(span.low() > high()) {
            return false;
        }

        return true;
    }
}
