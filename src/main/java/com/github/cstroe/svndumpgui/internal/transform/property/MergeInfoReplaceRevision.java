package com.github.cstroe.svndumpgui.internal.transform.property;

import com.github.cstroe.svndumpgui.generated.MergeInfoParser;
import com.github.cstroe.svndumpgui.generated.ParseException;

import java.util.function.Function;

public class MergeInfoReplaceRevision implements Function<String, String> {

    private final String mergePath;
    private final int oldRevision;
    private final int newRevision;

    public MergeInfoReplaceRevision(int oldRevision, int newRevision) {
        this(null, oldRevision, newRevision);
    }

    public MergeInfoReplaceRevision(String mergePath, int oldRevision, int newRevision) {
        this.mergePath = mergePath;
        this.oldRevision = oldRevision;
        this.newRevision = newRevision;
    }

    @Override
    public String apply(String s) {
        MergeInfoData data;
        try {
            data = MergeInfoParser.parse(s);
        } catch(ParseException ex) {
            throw new RuntimeException(ex);
        }

        for(MergeInfoData.Path path : data.getPaths()) {
            if(mergePath != null) {
                if(!mergePath.equals(path.getPath())) {
                    continue;
                }
            }
            for(MergeInfoData.Range range : path.getRanges()) {
                if(range.getFromRange() == oldRevision) {
                    range.setFromRange(newRevision);
                }
                if(range.getToRange() == oldRevision) {
                    range.setToRange(newRevision);
                }
            }
        }

        return data.toString();
    }
}
