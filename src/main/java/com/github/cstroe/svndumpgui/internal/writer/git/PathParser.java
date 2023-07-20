package com.github.cstroe.svndumpgui.internal.writer.git;

import org.javatuples.Triplet;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PathParser {
    private static final Pattern branchPattern = Pattern.compile("^branches/([^/]+)$", Pattern.MULTILINE);
    private static final Pattern isInBranchPattern = Pattern.compile("^branches/([^/]+)/(.+)$", Pattern.MULTILINE);
    private static final Pattern tagPattern = Pattern.compile("^tags/([^/]+)$", Pattern.MULTILINE);
    private static final Pattern isInTagPattern = Pattern.compile("^tags/([^/]+)/(.+)$", Pattern.MULTILINE);

    public Optional<Triplet<ChangeType, String, String>> parse(String path) {
        {
            Matcher m = branchPattern.matcher(path);
            if (m.matches()) {
                return Optional.of(Triplet.with(ChangeType.BRANCH_CREATE, m.group(1), null));
            }
        }{
            Matcher m = isInBranchPattern.matcher(path);
            if (m.matches()) {
                return Optional.of(Triplet.with(ChangeType.BRANCH, m.group(1), m.group(2)));
            }
        }{
            Matcher m = tagPattern.matcher(path);
            if (m.matches()) {
                return Optional.of(Triplet.with(ChangeType.TAG_CREATE, m.group(1), null));
            }
        }{
            Matcher m = isInTagPattern.matcher(path);
            if (m.matches()) {
                return Optional.of(Triplet.with(ChangeType.TAG, m.group(1), m.group(2)));
            }
        }
        return Optional.empty();
    }
}
