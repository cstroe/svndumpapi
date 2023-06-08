package com.github.cstroe.svndumpgui.cli;

import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.api.RepositoryValidator;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.transform.ClearRevision;
import com.github.cstroe.svndumpgui.internal.transform.NodeRemove;
import com.github.cstroe.svndumpgui.internal.transform.NodeRemoveByPath;
import com.github.cstroe.svndumpgui.internal.validate.PathCollisionValidator;
import com.github.cstroe.svndumpgui.internal.validate.TerminatingValidator;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriter;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriterNoBranching;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CliConsumer {
    public static void main(final String[] args) throws ParseException, UnsupportedEncodingException, FileNotFoundException {
        FileInputStream fis = new FileInputStream("/home/cosmin/Zoo/freshports/fp.svndump");
        AbstractRepositoryWriter gitWriter;
        try {
            gitWriter = new GitWriterNoBranching(null);
        } catch (GitAPIException | IOException ex) {
            ex.printStackTrace(System.err);
            return;
        }

        // bad commits (ingress/.git, ingress/DELETEME)
        RepositoryConsumer chain = new ClearRevision(5261, 5263);
        chain.continueTo(new NodeRemove(5264, "add", "ingress/DELETEME/commit-raw-find.sh"));
        chain.continueTo(new NodeRemove(5265, "add", "ingress/DELETEME/inject-retry-raw.sh"));
        chain.continueTo(new NodeRemove(5266, "add", "ingress/DELETEME/commit-raw-reinject.sh"));
        chain.continueTo(new NodeRemove(5267, "add", "ingress/DELETEME/commit-raw-find-by-revision.sh"));
        chain.continueTo(new ClearRevision(5268));

        // remove directories we don't need
        removePrefixes(chain, listOf(
                "scripts-fp2",
                "secure",
                "www",
                "include",

                "classes",

                "backend", // ignore backend/*
                "configuration", // ignore configuration/*
                "convert/tags",
                "daemontools/tags",
                "database-schema/tags",
                "dataconversion/tags",
                "db-conversion/tags",
                "develop/tags",
                "docs/tags",
                "fwatch/tags",
                "include/tags",
                "ingress/Verify/tags",
                "ingress/scripts/tags",
                "ingress/modules/tags",
                "ports/tags",
                "periodics/tags",
                "scripts/tags",
                "walkports/tags"
        ));

        chain.continueTo(new NodeRemoveByPath(
                Pattern.compile(
                        String.format("^(%s|%s/.+)$", "api.freshports.org", "api.freshports.org"),
                        Pattern.MULTILINE), true));

        try (FileOutputStream log = new FileOutputStream("/tmpfs/repo_summary.txt")) {
            RepositorySummary summary = new RepositorySummary();
            summary.writeTo(log);
            chain.continueTo(summary);
            RepositorySummary summaryAgain = new RepositorySummary();
            chain.continueTo(summaryAgain);
            chain.continueTo(gitWriter);
            RepositoryValidator pathCollisionValidator = new PathCollisionValidator();
            RepositoryValidator terminator = new TerminatingValidator(pathCollisionValidator);
            chain.continueTo(terminator);
            SvnDumpParser.consume(fis, chain);
            System.out.flush();
            System.err.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static NodeRemoveByPath removePrefix(String prefix) {
        return new NodeRemoveByPath(Pattern.compile(String.format("^(%s|%s/.+)$", prefix, prefix), Pattern.MULTILINE));
    }

    private static void removePrefixes(RepositoryConsumer chain, List<String> prefixes) {
        for(String prefix : prefixes) {
            chain.continueTo(removePrefix(prefix));
        }
    }

    private static List<String> listOf(String... element) {
        List<String> l = new ArrayList<>();
        l.addAll(Arrays.asList(element));
        return l;
    }
}
