package com.github.cstroe.svndumpgui.internal.writer.git;

import org.eclipse.jgit.lib.Ref;

import java.util.Objects;

public class SvnBranch {
    private final String name;
    private final String gitName;

    public static SvnBranch of(String name, String gitName) {
        return new SvnBranch(name, gitName);
    }

    private SvnBranch(String name, String gitName) {
        this.name = name;
        this.gitName = gitName;
    }

    public String getName() {
        return name;
    }

    public String getGitName() {
        return gitName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SvnBranch branch = (SvnBranch) o;
        return Objects.equals(name, branch.name) && Objects.equals(gitName, branch.gitName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, gitName);
    }

    @Override
    public String toString() {
        return "SvnBranch{" +
                "name='" + name + '\'' +
                ", gitName='" + gitName + '\'' +
                '}';
    }
}
