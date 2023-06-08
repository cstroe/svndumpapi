package com.github.cstroe.svndumpgui.conversions;


import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.transform.NodeRemoveByPath;
import com.github.cstroe.svndumpgui.internal.transform.PathChange;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriterNoBranching;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Contains code to convert the Freshports SVN repository to Git.
 */
public class Freshports {
    public static void main(String[] args) throws GitAPIException, IOException, ParseException {
        final String inputFile = "/home/cosmin/Zoo/freshports/fp.svndump";

//        createApiRepo(inputFile, "/tmpfs");
        createDaemontoolsRepo(inputFile, "/tmpfs");
    }

    /**
     * Create a git repo that contains just 'api.freshports.org/*' files.
     * @param inputSvnDump The SVN dump file for the Freshports SVN repo.
     */
    private static void createApiRepo(String inputSvnDump, String outputDir) throws GitAPIException, IOException, ParseException {
        String outputSubDir = outputDir + File.separator + "api_freshports_org";
        deleteDirectory(new File(outputSubDir));

        if (!new File(outputSubDir).mkdirs()) {
            throw new RuntimeException("Could not create directory: " + outputSubDir);
        }

        AbstractRepositoryWriter gitWriter = new GitWriterNoBranching(outputSubDir);

        RepositoryConsumer chain = new NodeRemoveByPath(
                Pattern.compile(
                        String.format("^(%s|%s/.+)$", "api.freshports.org", "api.freshports.org"),
                        Pattern.MULTILINE), true);

        chain.continueTo(new PathChange("api.freshports.org", ""));
        chain.continueTo(new PathChange("api.freshports.org/", ""));
        chain.continueTo(gitWriter);

        FileInputStream fis = new FileInputStream(inputSvnDump);
        SvnDumpParser.consume(fis, chain);
    }

    // from https://www.baeldung.com/java-delete-directory
    private static boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private static void createDaemontoolsRepo(String inputSvnDump, String outputDir) throws IOException, ParseException, GitAPIException {
        String outputSubDir = outputDir + File.separator + "daemontools";
        deleteDirectory(new File(outputSubDir));

        if (!new File(outputSubDir).mkdirs()) {
            throw new RuntimeException("Could not create directory: " + outputSubDir);
        }

        AbstractRepositoryWriter gitWriter = new GitWriterNoBranching(outputSubDir);

        RepositoryConsumer chain = new NodeRemoveByPath(
                Pattern.compile(
                        String.format("^(%s|%s/.+)$", "daemontools", "daemontools"),
                        Pattern.MULTILINE), true);

        chain.continueTo(new NodeRemoveByPath(
                Pattern.compile(
                        String.format("^(%s|%s/.+)$", "daemontools/tags", "daemontools/tags"),
                        Pattern.MULTILINE)));
        chain.continueTo(new PathChange("daemontools/", ""));
        chain.continueTo(new PathChange("daemontools", ""));
        chain.continueTo(new RepositorySummary());
        chain.continueTo(gitWriter);

        FileInputStream fis = new FileInputStream(inputSvnDump);
        SvnDumpParser.consume(fis, chain);
    }
}
