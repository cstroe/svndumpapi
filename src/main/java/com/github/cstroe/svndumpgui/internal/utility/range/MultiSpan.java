package com.github.cstroe.svndumpgui.internal.utility.range;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiSpan implements Cloneable {
    private List<Span> spans = new ArrayList<>();

    public MultiSpan() {}

    private MultiSpan(List<Span> spans) {
        this.spans = spans;
    }

    public void add(Span span) {
        for(Span currentSpan : spans) {
            if(currentSpan.merge(span)) {
                reduce();
                return;
            }
        }

        spans.add(span);
    }

    private void reduce() {
        MultiSpan multiSpan = new MultiSpan();
        for(Span currentSpan : spans) {
            multiSpan.add(currentSpan);
        }

        if(spans.size() > multiSpan.spans.size()) {
            spans = multiSpan.spans;
            reduce();
        }
    }

    public boolean contains(int value) {
        return spans.parallelStream().anyMatch(s -> s.contains(value));
    }

    public void cutoff(int value) {
        spans = spans.parallelStream().filter(s -> s.low() <= value).collect(Collectors.toList());
        spans.parallelStream().forEach(s -> s.cutoff(value));
    }

    List<Span> getSpans() {
        return spans;
    }

    @SuppressWarnings({"CloneDoesntCallSuperClone", "CloneDoesntDeclareCloneNotSupportedException"})
    @Override
    public MultiSpan clone() {
        List<Span> newSpans = new ArrayList<>(spans.size());
        for(Span currentSpan : spans) {
            newSpans.add(new SpanImpl(currentSpan.low(), currentSpan.high()));
        }
        return new MultiSpan(newSpans);
    }

    @Override
    public String toString() {
        return String.join(",", spans.parallelStream().map(Object::toString).collect(Collectors.toList()));
    }
}
