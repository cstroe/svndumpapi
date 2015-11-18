package com.github.cstroe.svndumpgui.internal.utility.range;

import java.util.ArrayList;
import java.util.List;

public class MultiSpan {
    private List<Span> spans = new ArrayList<>();

    public void add(Span span) {
        spans.add(span);
    }

    public boolean contains(int value) {
        return spans.parallelStream().anyMatch(s -> s.contains(value));
    }
}
