package com.github.cstroe.svndumpgui.conversions;


import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.transform.NodeRemoveByPath;
import com.github.cstroe.svndumpgui.internal.transform.PathChange;
import com.github.cstroe.svndumpgui.internal.utility.Tuple2;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import com.github.cstroe.svndumpgui.internal.writer.git.AuthorIdentities;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriterNoBranching;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Contains code to convert the Freshports SVN repository to Git.
 */
public class Freshports {
    private static final AuthorIdentities identities = new AuthorIdentities(Tuple2.of("Dan Langille", "dan@langille.org"))
            .add("dan", "Dan Langille", "dan@langille.org");

    public static void main(String[] args) throws GitAPIException, IOException, ParseException {
        final String inputFile = "/home/cosmin/Zoo/freshports/fp.svndump";

//         createSummary(inputFile, "/tmpfs");
//        createApiRepo(inputFile, "/tmpfs");
        createDaemontoolsRepo(inputFile, "/tmpfs");
    }

    private static void createSummary(String inputSvnDump, String outputDir) throws IOException, ParseException {
        File summaryFile = new File(outputDir + File.separator + "summary_original.txt");
        if (summaryFile.exists() && !summaryFile.delete()) {
            throw new RuntimeException("Could not delete output file: " + summaryFile.getAbsolutePath());
        }

        RepositorySummary repositorySummary = new RepositorySummary();
        repositorySummary.writeTo(Files.newOutputStream(summaryFile.toPath()));
        FileInputStream fis = new FileInputStream(inputSvnDump);
        SvnDumpParser.consume(fis, repositorySummary);
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

        AbstractRepositoryWriter gitWriter = new GitWriterNoBranching(outputSubDir, identities);

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
        File summaryFile = new File(outputDir + File.separator + "summary_daemontools.txt");
        if (summaryFile.exists() && !summaryFile.delete()) {
            throw new RuntimeException("Could not delete output file: " + summaryFile.getAbsolutePath());
        }

        String outputSubDir = outputDir + File.separator + "daemontools";
        deleteDirectory(new File(outputSubDir));

        if (!new File(outputSubDir).mkdirs()) {
            throw new RuntimeException("Could not create directory: " + outputSubDir);
        }

        RepositoryConsumer chain = keepOnly("^(daemontools|daemontools/.+)$");
        //chain.continueTo(remove("^(daemontools/tags|daemontools/tags/.+)$"));
        //chain.continueTo(new PathChange("daemontools/tags", "daemontools/branches"));
        chain.continueTo(new PathChange("daemontools/trunk/", ""));
        chain.continueTo(new PathChange("daemontools/trunk", ""));
        chain.continueTo(new PathChange("daemontools/", ""));
        chain.continueTo(new PathChange("daemontools", ""));

        RepositorySummary repositorySummary = new RepositorySummary();
        repositorySummary.writeTo(Files.newOutputStream(summaryFile.toPath()));
        chain.continueTo(repositorySummary);
        chain.continueTo(new RepositorySummary());

        AbstractRepositoryWriter gitWriter = new GitWriterNoBranching(outputSubDir, identities);
        chain.continueTo(gitWriter);

        FileInputStream fis = new FileInputStream(inputSvnDump);
        SvnDumpParser.consume(fis, chain);
    }

    private static NodeRemoveByPath keepOnly(String regex) {
        return new NodeRemoveByPath(Pattern.compile(regex, Pattern.MULTILINE), true);
    }

    private static NodeRemoveByPath remove(String regex) {
        return new NodeRemoveByPath(Pattern.compile(regex, Pattern.MULTILINE));
    }
}
