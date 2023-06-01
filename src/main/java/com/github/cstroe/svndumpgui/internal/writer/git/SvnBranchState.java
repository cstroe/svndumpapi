package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.Node;

import java.util.*;

public class SvnBranchState {
    private final List<SvnBaseDir> baseDirs = new ArrayList<>();
    private final Map<SvnBaseDir, List<SvnBranch>> branches = new HashMap<>();

    public Optional<SvnBaseDir> getBranchDir(String path) {
        return baseDirs.stream()
                .filter(basedir -> basedir.isFileInBranch(path))
                .findFirst();
    }

    public boolean isBranchPath(String path) {
        return getBranchDir(path).isPresent();
    }

    public Optional<SvnBaseDir> getBranchDir(Node node) {
        return node.getPath().flatMap(s -> baseDirs.stream()
            .filter(basedir -> basedir.isFileInBranch(s))
            .findFirst());
    }

    public Optional<SvnBaseDir> getTagsDir(Node node) {
        return node.getPath().flatMap(s -> baseDirs.stream()
                .filter(basedir -> basedir.isFileInTags(s))
                .findFirst());
    }

    public boolean isInBranch(Node node) {
        return getBranchDir(node).isPresent();
    }

    public void addBaseDir(SvnBaseDir baseDir) {
        baseDirs.add(baseDir);
        branches.put(baseDir, new ArrayList<>());
    }

    public void addBranch(SvnBaseDir baseDir, SvnBranch branch) {
        List<SvnBranch> currentBranches = branches.getOrDefault(baseDir, new ArrayList<>());
        currentBranches.add(branch);
        branches.put(baseDir, currentBranches);
    }

    public Optional<SvnBranch> getBranch(Node node) {
        Optional<String> maybePath = node.getPath();
        if (!maybePath.isPresent()) {
            return Optional.empty();
        }
        String path = maybePath.get();
        return getBranchDir(node)
                .flatMap(baseDir ->
                        branches.get(baseDir).stream()
                                .filter(branch -> {
                                    String branchPath = baseDir.getBranchesDir() + "/" + branch.getName() + "/";
                                    return path.startsWith(branchPath);
                                }).findFirst());
    }

    public boolean isInTags(Node node) {
        return node.getPath()
                .filter(path -> path.split("/").length > 1 && "tags".equals(path.split("/")[1]))
                .isPresent();
        //return getTagsDir(node).isPresent();
    }
}
