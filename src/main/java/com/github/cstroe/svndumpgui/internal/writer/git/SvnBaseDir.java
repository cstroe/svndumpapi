package com.github.cstroe.svndumpgui.internal.writer.git;

import java.util.Objects;
import java.util.Optional;

public class SvnBaseDir {
    private final String baseDir;
    private final String trunkDir;
    private final String branchesDir;
    private final String tagsDir;

    public static SvnBaseDir of(String baseDir) {
        return new SvnBaseDir(
                baseDir,
                baseDir + "/trunk",
                baseDir + "/branches",
                baseDir + "/tags"
        );
    }

    private SvnBaseDir(String baseDir, String trunkDir, String branchesDir, String tagsDir) {
        this.baseDir = baseDir;
        this.trunkDir = trunkDir;
        this.branchesDir = branchesDir;
        this.tagsDir = tagsDir;
    }

    public String getBaseDir() {
        return baseDir;
    }

    public String getTrunkDir() {
        return trunkDir;
    }

    public String getBranchesDir() {
        return branchesDir;
    }

    public String getTagsDir() {
        return tagsDir;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SvnBaseDir that = (SvnBaseDir) o;
        return Objects.equals(baseDir, that.baseDir) && Objects.equals(trunkDir, that.trunkDir) && Objects.equals(branchesDir, that.branchesDir) && Objects.equals(tagsDir, that.tagsDir);
    }

    @Override
    public int hashCode() {
        return Objects.hash(baseDir, trunkDir, branchesDir, tagsDir);
    }

    @Override
    public String toString() {
        return "SvnBaseDir{" +
                "baseDir='" + baseDir + '\'' +
                '}';
    }

    public boolean isFileInBranch(String name) {
        return name.startsWith(getBranchesDir() + "/");
    }

    public Optional<String> stripBranchPrefix(String path) {
        if (path == null ||
                path.length() <= getBranchesDir().length() + 1 ||
                !path.startsWith(getBranchesDir() + "/")) {
            return Optional.empty();
        }
        return Optional.of(path.substring(getBranchesDir().length() + 1));
    }

    public boolean isFileInTags(String name) {
        return name.startsWith(getTagsDir() + "/");
    }
}
