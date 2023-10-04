package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.generated.ParseException;
import com.github.cstroe.svndumpgui.generated.SvnDumpParser;
import com.github.cstroe.svndumpgui.internal.utility.SvnDumpCharStream;
import com.github.cstroe.svndumpgui.internal.utility.Tuple2;
import org.apache.sshd.common.util.io.IoUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GitWriterNoBranchingTest {
    private final AuthorIdentities identities = new AuthorIdentities(Tuple2.of("Default User", "default@user.com"))
            .add("cosmin", "User Name", "username@example.com");

    @Test(expected = RuntimeException.class)
    public void throwsExceptionIfDirectoryDoesNotExist() throws IOException, GitAPIException {
        Path tempDir = Files.createTempDirectory("test-svndumpadmin-");
        new GitWriterNoBranching(tempDir.toFile().getAbsolutePath() + File.separator + "does_not_exist", identities);
    }

    @Test(expected = RuntimeException.class)
    public void throwsExceptionIfPathIsAFile() throws IOException, GitAPIException {
        File tempFile = File.createTempFile("test-svndumpadmin-", "");
        new GitWriterNoBranching(tempFile.getAbsolutePath(), identities);
    }

    @Test(expected = RuntimeException.class)
    public void checkExists() throws IOException, GitAPIException {
        File tempFile = Files.createTempFile("test-snvdumpadmin-", ".test").toFile();
        new GitWriterNoBranching(tempFile.getAbsolutePath(), identities);
    }


    @Test
    public void multipleChange() throws IOException, GitAPIException, ParseException {
        String dumpFilePath = "dumps/add_and_multiple_change.dump";

        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(dumpFilePath);

        File tempDir = Files.createTempDirectory("test-svndumpadmin-").toFile();
        assertTrue(tempDir.exists() && tempDir.isDirectory());
        System.out.println(tempDir.getAbsolutePath());
        GitWriterNoBranching gitWriter = new GitWriterNoBranching(tempDir.getAbsolutePath(), identities);

        SvnDumpParser parser = new SvnDumpParser(new SvnDumpCharStream(is));
        parser.Start(gitWriter);

        File outputFile = new File(tempDir.getAbsoluteFile() + File.separator + "file1.txt");
        List<String> fileContent = IoUtils.readAllLines(Files.newInputStream(outputFile.toPath()));
        assertEquals(1, fileContent.size());
        assertEquals("changed file a third time", fileContent.get(0));

        try(Git repo = Git.open(tempDir)) {
            Iterable<RevCommit> commits = repo.log().call();
            List<RevCommit> commitList = new ArrayList<>();
            for(RevCommit c : commits) {
                commitList.add(c);
            }

            assertEquals(5, commitList.size());
            assertEquals("Changed file a third time.\nSVN revision: 4", commitList.get(0).getFullMessage());
            assertEquals("Changed file again.\nSVN revision: 3", commitList.get(1).getFullMessage());
            assertEquals("Changed file.\nSVN revision: 2", commitList.get(2).getFullMessage());
            assertEquals("Initial commit.\nSVN revision: 1", commitList.get(3).getFullMessage());
            assertEquals("Initial commit.\nSVN revision: 0", commitList.get(4).getFullMessage());

            repo.checkout().setName(commitList.get(3).getName()).call(); // checkout revision 1
            List<String> fileContent_r1 = IoUtils.readAllLines(Files.newInputStream(outputFile.toPath()));
            assertEquals(1, fileContent_r1.size());
            assertEquals("this is a test file", fileContent_r1.get(0));
        }
    }
}