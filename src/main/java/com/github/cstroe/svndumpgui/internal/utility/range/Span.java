package com.github.cstroe.svndumpgui.internal.utility.range;

public interface Span {
    int NEGATIVE_INFINITY = Integer.MIN_VALUE;
    int POSITIVE_INFINITY = Integer.MAX_VALUE;
    int low();
    int high();
    void low(int value);
    void high(int value);

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

    /**
     * Merge with another span.
     * @param otherSpan Must overlap with this span.
     * @return true if merge happened, false if it didn't
     */
    default boolean merge(Span otherSpan) {
        if(!overlaps(otherSpan)) {
            return false;
        }

        low(Math.min(low(), otherSpan.low()));
        high(Math.max(high(), otherSpan.high()));

        return true;
    }

    default void cutoff(int value) {
        if(low() > value) {
            throw new IllegalArgumentException("cannot cutoff to " + value + " with span: " + this);
        }

        if(high() > value) {
            high(value);
        }
    }
}
