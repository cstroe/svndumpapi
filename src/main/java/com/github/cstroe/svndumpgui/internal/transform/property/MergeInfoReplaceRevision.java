package com.github.cstroe.svndumpgui.internal.transform.property;

import com.github.cstroe.svndumpgui.generated.MergeInfoParser;
import com.github.cstroe.svndumpgui.generated.ParseException;

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
        MergeInfoData data;
        try {
            data = MergeInfoParser.parse(s);
        } catch(ParseException ex) {
            throw new RuntimeException(ex);
        }

        for(MergeInfoData.Path path : data.getPaths()) {
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
