package com.github.cstroe.svndumpgui.internal.transform.property;

import java.util.function.Function;

public class MergeInfoReplaceRevision implements Function<String, String> {

    private final int oldRevision;
    private final int newRevision;

    public MergeInfoReplaceRevision(int oldRevision, int newRevision) {
        this.oldRevision = oldRevision;
        this.newRevision = newRevision;
    }

    @Override
    public String apply(String s) {
        return null;
    }
}
