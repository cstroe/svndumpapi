package com.github.cstroe.svndumpgui.conversions;


import com.github.cstroe.svndumpgui.api.RepositoryConsumer;
import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.transform.ClearRevision;
import com.github.cstroe.svndumpgui.internal.transform.NodeRemoveByPath;
import com.github.cstroe.svndumpgui.internal.transform.PathChange;
import com.github.cstroe.svndumpgui.internal.utility.Tuple2;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import com.github.cstroe.svndumpgui.internal.writer.RepositorySummary;
import com.github.cstroe.svndumpgui.internal.writer.git.AuthorIdentities;
import com.github.cstroe.svndumpgui.internal.writer.git.GitRepo;
import com.github.cstroe.svndumpgui.internal.writer.git.GitRepoImpl;
import com.github.cstroe.svndumpgui.internal.writer.git.GitWriterNoBranching;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.javatuples.Triplet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
//        createDaemontoolsRepo(inputFile, "/tmpfs");
        validateDaemontoolsRepo();

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
        chain.continueTo(new PathChange("tags/fp-listen-git1.0.0", "tags/fp-listen-git-1.0.0"));
        chain.continueTo(new ClearRevision(5570));

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

    private static void validateDaemontoolsRepo() throws GitAPIException {
        String gitPath = "/tmpfs/daemontools";
        String svnPath = "/home/cosmin/Zoo/freshports/checkouts/daemontools";
        String tagsDir = svnPath + File.separator + "tags";

        GitRepoImpl repo = new GitRepoImpl(new File(gitPath));
        repo.open().map(e -> {throw e;});
        List<Ref> gitTags = repo.getGit().tagList().call();

        File[] svnTags = new File(tagsDir).listFiles();
        if (svnTags == null) {
            throw new RuntimeException("cannot list files in: " + tagsDir);
        }

        System.out.println("Found " + svnTags.length + " SVN tags.");

        Triplet<Set<String>, Set<String>, Set<String>> tags = compareTags(gitTags, svnTags);

        for (File tag : svnTags) {
            // TODO: Check the file SHAs of each tag
        }
    }

    private static Triplet<Set<String>, Set<String>, Set<String>> compareTags(
            List<Ref> gitTags, File[] svnTags) {
        Set<String> svnTagsSet = new HashSet<>();
        for(File tag : svnTags) {
            svnTagsSet.add(tag.getName());
        }

        Set<String> gitTagsSet = new HashSet<>();
        for(Ref tag : gitTags) {
            String name = tag.getName();
            String prefix = "refs/tags/";
            if (name.startsWith(prefix)) {
                gitTagsSet.add(name.substring(prefix.length()));
            }
        }

        Set<String> commonTags = new HashSet<>();

        // tags that are in SVN only
        Set<String> svnOnlyTags = new HashSet<>();
        for (String tag : svnTagsSet) {
            if (!gitTagsSet.contains(tag)) {
                svnOnlyTags.add(tag);
            } else {
                commonTags.add(tag);
            }
        }
        if (!svnOnlyTags.isEmpty()) {
            System.out.println("SVN only tags:");
            for(String tag : svnOnlyTags) {
                System.out.println(tag);
            }
        }

        // tags that are in SVN only
        Set<String> gitOnlyTags = new HashSet<>();
        for (String tag : gitTagsSet) {
            if (!svnTagsSet.contains(tag)) {
                gitOnlyTags.add(tag);
            }
        }
        if (!gitOnlyTags.isEmpty()) {
            System.out.println("git only tags:");
            for(String tag : gitOnlyTags) {
                System.out.println(tag);
            }
        }
        return Triplet.with(gitOnlyTags, svnOnlyTags, commonTags);
    }
}
