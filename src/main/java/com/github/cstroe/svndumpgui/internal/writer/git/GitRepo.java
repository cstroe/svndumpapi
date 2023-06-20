package com.github.cstroe.svndumpgui.internal.writer.git;

import com.leakyabstractions.result.Result;

import java.util.Optional;

/**
 * An interface with a Git repo.
 */
public interface GitRepo {
    /**
     * Initialize the git repository.  This is the equivalent of `git init`.
     */
    Optional<RuntimeException> init();

    Optional<RuntimeException> open();
}
