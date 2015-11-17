package com.github.cstroe.svndumpgui.internal.utility;

// based on example from: https://docs.oracle.com/javase/tutorial/essential/io/examples/Copy.java

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemLoopException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class FileOperations {

    public static class RecursiveCopier implements FileVisitor<Path> {
        private final Path source;
        private final Path target;

        public RecursiveCopier(Path source, Path target) {
            this.source = source;
            this.target = target;
        }

        /**
         * Create the directory before visiting files inside it.
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            Path newdir = target.resolve(source.relativize(dir));
            try {
                Files.copy(dir, newdir, COPY_ATTRIBUTES);
            } catch (FileAlreadyExistsException x) {
                // ignore
            } catch (IOException x) {
                throw new RuntimeException("Unable to create: " + newdir, x);
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
            Path relativeTarget = target.resolve(source.relativize(file));
            if (Files.notExists(relativeTarget)) {
                try {
                    Files.copy(file, relativeTarget, COPY_ATTRIBUTES, REPLACE_EXISTING);
                } catch (IOException x) {
                    throw new RuntimeException("Unable to copy: " + file, x);
                }
            }
            return CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
            return CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc) {
            if (exc instanceof FileSystemLoopException) {
                throw new RuntimeException("cycle detected: " + file);
            } else {
                throw new RuntimeException("Unable to copy: " + file, exc);
            }
        }
    }


    // from http://www.adam-bien.com/roller/abien/entry/java_7_deleting_recursively_a
    public static class RecursiveDeleter extends SimpleFileVisitor<Path> {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
            Files.delete(file);
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException ex) throws IOException {
            Files.delete(dir);
            return FileVisitResult.CONTINUE;
        }
    }
}

