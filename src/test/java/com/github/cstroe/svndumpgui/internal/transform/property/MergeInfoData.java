package com.github.cstroe.svndumpgui.internal.transform.property;

import java.util.ArrayList;
import java.util.List;

public class MergeInfoData {

    public static class MergeInfoPath {
        private List<MergeInfoRange> ranges = new ArrayList<>();

        public void addRange(MergeInfoRange range) {
            ranges.add(range);
        }

        public List<MergeInfoRange> getRanges() {
            return ranges;
        }
    }

    public static class MergeInfoRange {
        public static final int NOT_SET = -1;
        private int fromRange;
        private int toRange;

        public MergeInfoRange(int fromRange) {
            this(fromRange, NOT_SET);
        }

        public MergeInfoRange(int fromRange, int toRange) {
            this.fromRange = fromRange;
            this.toRange = toRange;
        }

        public int getFromRange() {
            return fromRange;
        }

        public void setFromRange(int fromRange) {
            this.fromRange = fromRange;
        }

        public int getToRange() {
            return toRange;
        }

        public void setToRange(int toRange) {
            this.toRange = toRange;
        }
    }
}
