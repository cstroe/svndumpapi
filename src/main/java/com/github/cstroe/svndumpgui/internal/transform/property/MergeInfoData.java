package com.github.cstroe.svndumpgui.internal.transform.property;

import java.util.ArrayList;
import java.util.List;

public class MergeInfoData {
    private List<Path> mergePaths = new ArrayList<>();

    public List<Path> getPaths() {
        return mergePaths;
    }

    public void addPath(Path path) {
        this.mergePaths.add(path);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        mergePaths.forEach(builder::append);
        return builder.toString();
    }

    public static class Path {
        private String path;
        private List<Range> ranges = new ArrayList<>();

        public Path(String path) {
            this.path = path;
        }

        public String getPath() {
            return path;
        }

        public void addRange(Range range) {
            ranges.add(range);
        }

        public List<Range> getRanges() {
            return ranges;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append(path).append(":");
            boolean firstRange = true;
            for(Range range : ranges) {
                if(firstRange) {
                    firstRange = false;
                } else {
                    builder.append(",");
                }
                builder.append(range);
            }
            builder.append("\n");
            return builder.toString();
        }
    }

    public static class Range {
        public static final int NOT_SET = -1;
        private int fromRange;
        private int toRange;

        public Range(int fromRange) {
            this(fromRange, NOT_SET);
        }

        public Range(int fromRange, int toRange) {
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

        @Override
        public String toString() {
            if(toRange == NOT_SET) {
                return Integer.toString(fromRange);
            } else {
                return fromRange + "-" + toRange;
            }
        }
    }
}
