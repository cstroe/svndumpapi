package com.github.cstroe.svndumpgui.internal.writer.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.util.Optional;

public class GitRepoImpl implements GitRepo {
    private final File gitDir;
    private Git git;

    public GitRepoImpl(File gitDir) {
        this.gitDir = gitDir;
    }

    @Override
    public Optional<RuntimeException> init() {
        if (!this.gitDir.exists()) {
            return Optional.of(new RuntimeException("Directory does not exist: " + this.gitDir.getAbsolutePath()));
        }

        try {
            this.git = Git.init()
                    .setDirectory(this.gitDir)
                    .call();
            return Optional.empty();
        } catch (GitAPIException e) {
            return Optional.of(new RuntimeException(e));
        }
    }
}
