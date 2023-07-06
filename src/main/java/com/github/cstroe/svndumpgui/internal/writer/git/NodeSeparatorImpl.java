package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;
import org.javatuples.Quartet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NodeSeparatorImpl implements NodeSeparator {
    private static final Pattern branchPattern = Pattern.compile("^branches/([^/]+)$", Pattern.MULTILINE);
    private static final Pattern isInBranchPattern = Pattern.compile("^branches/([^/]+)/(.+)$", Pattern.MULTILINE);
    private static final Pattern tagPattern = Pattern.compile("^tags/([^/]+)$", Pattern.MULTILINE);
    private static final Pattern isInTagPattern = Pattern.compile("^tags/([^/]+)/(.+)$", Pattern.MULTILINE);
    private final String mainBranch;

    public NodeSeparatorImpl() {
        this("main");
    }

    public NodeSeparatorImpl(String mainBranch) {
        this.mainBranch = mainBranch;
    }

    @Override
    public List<Quartet<ChangeType, String, String, Node>> separate(List<Node> nodes) {
        List<Quartet<ChangeType, String, String, Node>> list = new ArrayList<>();

        for (Node n : nodes) {
            Optional<String> maybePath = n.getPath();
            if (!maybePath.isPresent()) {
                throw new RuntimeException("Node doesn't have a path: " + n);
            }

            Matcher branchMatcher = branchPattern.matcher(maybePath.get());
            if (branchMatcher.matches()) {
                String branchName = branchMatcher.group(1);
                list.add(Quartet.with(ChangeType.BRANCH_CREATE, branchName, null, n));
                continue;
            }

            Matcher inBranchMatcher = isInBranchPattern.matcher(maybePath.get());
            if (inBranchMatcher.matches()) {
                String branchName = inBranchMatcher.group(1);
                String branchPath = inBranchMatcher.group(2);
                list.add(Quartet.with(ChangeType.BRANCH, branchName, branchPath, n));
                continue;
            }

            Matcher tagMatcher = tagPattern.matcher(maybePath.get());
            if (tagMatcher.matches()) {
                String tagName = tagMatcher.group(1);
                list.add(Quartet.with(ChangeType.TAG_CREATE, tagName, null, n));
                continue;
            }

            Matcher inTagMatcher = isInTagPattern.matcher(maybePath.get());
            if (inTagMatcher.matches()) {
                String tagName = inTagMatcher.group(1);
                String tagPath = inTagMatcher.group(2);
                list.add(Quartet.with(ChangeType.TAG, tagName, tagPath, n));
                continue;
            }

            list.add(Quartet.with(ChangeType.TRUNK, mainBranch, maybePath.get(), n));
        }
        return list;
    }
}
