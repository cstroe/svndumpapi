package com.github.cstroe.svndumpgui.internal.transform.property;

import java.util.ArrayList;
import java.util.List;

public class MergeInfoData {
    private List<Path> mergePaths = new ArrayList<>();
    private boolean trailingNewLine = true;

    public List<Path> getPaths() {
        return mergePaths;
    }

    public void addPath(Path path) {
        this.mergePaths.add(path);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        mergePaths.forEach(path -> {
            builder.append(path);
            builder.append("\n");
        });
        if(builder.length() > 0 && !trailingNewLine) {
            builder.deleteCharAt(builder.length() - 1);
        }
        return builder.toString();
    }

    public void setTrailingNewLine(boolean trailingNewLine) {
        this.trailingNewLine = trailingNewLine;
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

        public void setPath(String path) {
            this.path = path;
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
            return builder.toString();
        }
    }

    public static class Range {
        public static final int NOT_SET = -1;
        private int fromRange;
        private int toRange;
        private final boolean nonInheritable;

        public Range(int fromRange) {
            this(fromRange, NOT_SET);
        }

        public Range(int fromRange, boolean nonInheritable) {
            this(fromRange, NOT_SET, nonInheritable);
        }

        public Range(int fromRange, int toRange) {
            this(fromRange, toRange, false);
        }

        public Range(int fromRange, int toRange, boolean nonInheritable) {
            this.fromRange = fromRange;
            this.toRange = toRange;
            this.nonInheritable = nonInheritable;
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

        public boolean isNonInheritable() {
            return nonInheritable;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            if(toRange == NOT_SET) {
                builder.append(Integer.toString(fromRange));
            } else {
                builder.append(fromRange).append("-").append(toRange);
            }

            if(nonInheritable) {
                builder.append("*");
            }

            return builder.toString();
        }
    }
}
