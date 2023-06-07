package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;

import java.io.*;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Git writer that treats all SVN paths as files.  No branches, no tags.
 */
public class GitWriterNoBranching extends AbstractRepositoryWriter {
    private final File gitDir;
    private final Git git;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
    private final int startFromRev;

    private Revision currentRevision;
    private Node currentNode;
    private Ref gitBranchForSvnRevision;
    private Map<Integer, List<String>> svnRevisionToGitHash = new HashMap<>(9182);

    public GitWriterNoBranching() throws IOException, GitAPIException {
        File tmpDir = Files.createTempDirectory("svndumpadmin-git-").toFile();
        String dirName = tmpDir.getName();
        this.gitDir = new File("/tmpfs/");
        this.startFromRev = 0;

        System.out.println("Git directory: " + this.gitDir.getAbsolutePath());
        this.git = Git.init()
                .setDirectory(this.gitDir)
                .call();
    }

    public GitWriterNoBranching(String gitDir, int startFromRev) throws GitAPIException, IOException {
        if (gitDir.startsWith("/tmpfs/")) {
            throw new RuntimeException("Please remove the '/tmpfs/' prefix from your git directory.");
        }

        File tmp = new File(gitDir);
        String dirName = tmp.getName();
        this.gitDir = new File("/tmpfs/" + dirName);
        this.startFromRev = startFromRev;

        System.out.println("Git directory: " + this.gitDir.getAbsolutePath());
        this.git = Git.open(this.gitDir);

        Pattern revCommitMsgPattern = Pattern.compile("(SVN revision: [0-9]+)");
        for(RevCommit commit : this.git.log().all().call()) {
            String message = commit.getFullMessage();
            Matcher m = revCommitMsgPattern.matcher(message);
            if (m.find()) {
                String revisionFragment = m.group(1);
                String[] splits = revisionFragment.split(": ");
                int revisionNumber = Integer.parseInt(splits[1]);
                svnRevisionToGitHash
                        .computeIfAbsent(revisionNumber, s -> new ArrayList<>())
                        .add(commit.getName());
            }
        }
        ps().println("Found " + svnRevisionToGitHash.size() + " revisions.");
        //throw new RuntimeException("boom");
    }

    @Override
    public void consume(Revision revision) {
        super.consume(revision);
        if (revision.getNumber() < startFromRev) {
            currentRevision = null;
            return;
        }
        currentRevision = revision;
    }

    @Override
    public void consume(Node node) {
        super.consume(node);
        if (currentRevision != null) {
            currentNode = node;
        }
    }

    @Override
    public void endNode(Node node) {
        super.endNode(node);
        if (currentRevision != null) {
            currentRevision.addNode(node);
            node.setRevision(currentRevision);
        }
    }

    @Override
    public void consume(ContentChunk chunk) {
        super.consume(chunk);
        if (currentNode != null) {
            currentNode.addFileContentChunk(chunk);
        }
    }

    @Override
    public void finish() {
        super.finish();
        git.close();
    }

    /**
     * Handle translation of this SVN revision into Git.
     */
    @Override
    public void endRevision(Revision revision) {
        super.endRevision(revision);

        if (currentRevision == null) {
            ps().println(String.format("[%5s] Revision is skipped.", revision.getNumber()));
            return;
        }

        if (revision.getNumber() == 0) {
            try {
                Process touch = new ProcessBuilder()
                        .command("touch", ".gitignore")
                        .directory(gitDir)
                        .start();
                int retVal = touch.waitFor();
                if (retVal != 0) {
                    throw new RuntimeException("could not run: touch .gitignore, return value = " + retVal);
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            try {
                final PersonIdent ident = getIdent(revision);
                gitAddAll();
                git.commit()
                        .setAuthor(ident)
                        .setCommitter(ident)
                        .setMessage("Initial commit. (SVN revision " + revision.getNumber() + ")")
                        .call();
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }

            ps().println(String.format("[%5s] Created an initial commit.", revision.getNumber()));
            return;
        }

        // start a new branch
        String gitBranchName = "rev-" + revision.getNumber();
        try {
            Ref gitBranchCreated = git.checkout().setName(gitBranchName).setCreateBranch(true).call();
            this.gitBranchForSvnRevision = gitBranchCreated;
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        ps().println(String.format("[%5d] Created branch %s.", revision.getNumber(), gitBranchName));

        List<Node> nodes = revision.getNodes();
        nodes.forEach(this::processNode);
        doCommit(revision);
    }

    private void processNode(Node node) {
        if (node.isDir() && node.getHeaders().get(NodeHeader.COPY_FROM_REV) == null) {
            ps().println(String.format("[%5s] Skipping empty directory node: %s", node.getRevision().get().getNumber(), node.getPath().get()));
            return;
        } else if (node.isDir()) {
            createDirectoryFromHistory(node);
            return;
        }

        // write the file
        Optional<String> maybeAction = node.getAction();
        if (!maybeAction.isPresent()) {
            return;
        }
        switch (maybeAction.get()) {
            case "add":
                addNewFile(node);
                return;
            case "change":
                changeExistingFile(node);
                return;
            case "delete":
                deleteNode(node);
                return;
            case "replace":
                replaceNode(node);
                return;
            default:
                throw new RuntimeException("Can't handle node action: " + maybeAction.get());
        }
    }

    private void replaceNode(Node node) {
        changeExistingFile(node);
    }

    private void createDirectoryFromHistory(Node node) {
        String copyFromRev = node.getHeaders().get(NodeHeader.COPY_FROM_REV);
        String copyFromPath = node.getHeaders().get(NodeHeader.COPY_FROM_PATH);
        if (copyFromRev == null || copyFromPath == null) {
            throw new RuntimeException("A revision is missing a path");
        }
        Integer copyFromRevision = Integer.valueOf(node.get(NodeHeader.COPY_FROM_REV));
        List<String> gitShas = svnRevisionToGitHash.get(copyFromRevision);
        if (gitShas == null) {
            throw new RuntimeException("Could not find a commit for revision: " + copyFromRevision);
        }
        if (gitShas.size() != 1) {
            throw new RuntimeException("Not the correct number of git shas: " + gitShas.size());
        }

        String copyFromGitSha = gitShas.get(0);

        try {
            ps().println(String.format("[%5d] Restoring directory from: %s:%s at %s",
                    node.getRevision().get().getNumber(), copyFromPath, copyFromRev, copyFromGitSha));

            Process gitLsTree = new ProcessBuilder(
                    "/usr/bin/git", "ls-tree", "-r", copyFromGitSha, copyFromPath)
                    .directory(this.gitDir)
                    .start();

            List<List<String>> result = new BufferedReader(new InputStreamReader(gitLsTree.getInputStream()))
                    .lines()
                    .map(l -> l.split("\t"))
                    .map(l ->
                            Stream.concat(
                                    Arrays.stream(l[0].split(" ")),
                                    Arrays.stream(new String[] { l[1] })
                            ).collect(Collectors.toList())
                    )
                    .collect(Collectors.toList());

            ps().println(String.format("[%5d] Processing %d files.",
                    node.getRevision().get().getNumber(), result.size()));
            Iterator<List<String>> resultIter = result.listIterator();
            for (int i = 0; i < result.size(); i++) {
                List<String> fileInfo = resultIter.next();
                String originalFile = fileInfo.get(3);
                String newFile = node.getPath().get() + File.separator + originalFile.substring(copyFromPath.length() + 1);
                File parentDir = new File(gitDir.getAbsolutePath() + File.separator + newFile).getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new RuntimeException("could not create directory: " + parentDir.getAbsolutePath());
                };
                Process gitMvCommand = new ProcessBuilder(
                        "/usr/bin/git", "mv", originalFile, newFile
                ).directory(this.gitDir).start();
                int retVal = gitMvCommand.waitFor();
                if (retVal != 0) {
                    throw new RuntimeException("could not execute: git mv " + originalFile + " " + newFile + ", return value: " + retVal);
                }

                final Revision revision = node.getRevision().get();
                final PersonIdent ident = getIdent(revision);
                git.commit()
                        .setAuthor(ident)
                        .setCommitter(ident)
                        .setMessage("move [" + originalFile + "] to [" + newFile + "] " + " (SVN revision " + revision.getNumber() + ")")
                        .call();

                git.checkout().setStartPoint("HEAD~").addPath(originalFile).call();

                git.commit()
                        .setAuthor(ident)
                        .setCommitter(ident)
                        .setMessage("restore [" + originalFile + "] (SVN revision " + revision.getNumber() + ")")
                        .call();

                if (i % 50 == 0) {
                    ps().println(String.format("[%5d] Processing file %d of %d.",
                            node.getRevision().get().getNumber(), i + 1, result.size()));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ps().println(String.format("[%5d] Finished processing files.", node.getRevision().get().getNumber()));
    }

    private void addNewFile(Node node) {
        if (node.getHeaders().get(NodeHeader.COPY_FROM_REV) != null) {
            addNewFileFromHistory(node);
            return;
        }
        String nodePath = node.getHeaders().get(NodeHeader.PATH);
        if (nodePath == null) {
            throw new RuntimeException("missing path for node add");
        }

        String absolutePath = gitDir.getAbsolutePath() + File.separator + nodePath;

        File newFile = new File(absolutePath);
        newFile.getParentFile().mkdirs();

        try(FileOutputStream fos = new FileOutputStream(newFile)){
            fos.write(node.getByteContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final Revision revision = node.getRevision().get();
        final PersonIdent ident = getIdent(revision);
        try {
            gitAddAll();
            git.commit()
                    .setAuthor(ident)
                    .setCommitter(ident)
                    .setMessage("add new file [" + nodePath + "] (SVN revision " + revision.getNumber() + ")")
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewFileFromHistory(Node node) {
        String copyFromRev = node.getHeaders().get(NodeHeader.COPY_FROM_REV);
        String copyFromPath = node.getHeaders().get(NodeHeader.COPY_FROM_PATH);
        if (copyFromRev == null || copyFromPath == null) {
            throw new RuntimeException("A revision is missing a path");
        }
        Integer copyFromRevision = Integer.valueOf(node.get(NodeHeader.COPY_FROM_REV));
        List<String> gitShas = svnRevisionToGitHash.get(copyFromRevision);
        if (gitShas.size() != 1) {
            throw new RuntimeException("Not the correct number of git shas: " + gitShas.size());
        }

        String copyFromGitSha = gitShas.get(0);

        try {
            ps().println(String.format("[%5d] Restoring file %s@%s from %s",
                    node.getRevision().get().getNumber(), copyFromPath, copyFromRev, copyFromGitSha));

            Process gitLsTree = new ProcessBuilder(
                    "/usr/bin/git", "ls-tree", "-r", copyFromGitSha, copyFromPath)
                    .directory(this.gitDir)
                    .start();

            List<List<String>> result = new BufferedReader(new InputStreamReader(gitLsTree.getInputStream()))
                    .lines()
                    .map(l -> l.split("\t"))
                    .map(l ->
                            Stream.concat(
                                    Arrays.stream(l[0].split(" ")),
                                    Arrays.stream(new String[] { l[1] })
                            ).collect(Collectors.toList())
                    )
                    .collect(Collectors.toList());

            final Revision revision = node.getRevision().get();
            final PersonIdent ident = getIdent(revision);

            for (List<String> file : result) {
                String originalFile = file.get(3);
                String newFile = node.getPath().get();

                File absoluteNewFile = new File(gitDir.getAbsolutePath() + File.separator + newFile);
                if (!absoluteNewFile.exists()) {
                    Process gitCheckout = new ProcessBuilder()
                            .command("/usr/bin/git", "checkout", copyFromGitSha, "--", copyFromPath)
                            .directory(gitDir)
                            .start();

                    int gitCheckoutRetVal = gitCheckout.waitFor();
                    if (gitCheckoutRetVal != 0) {
                        throw new RuntimeException("could not execute: git checkout " + copyFromGitSha + " -- " + copyFromPath + ", return value: " + gitCheckoutRetVal);
                    }

                    git.commit()
                            .setAuthor(ident)
                            .setCommitter(ident)
                            .setMessage("restore [" + originalFile + "] (SVN revision " + revision.getNumber() + ")")
                            .call();

                }

                File parentDir = absoluteNewFile.getParentFile();
                if (!parentDir.exists() && !parentDir.mkdirs()) {
                    throw new RuntimeException("could not create directory: " + parentDir.getAbsolutePath());
                }


                Process gitMvCommand = new ProcessBuilder(
                        "/usr/bin/git", "mv", originalFile, newFile
                ).directory(this.gitDir).start();
                int retVal = gitMvCommand.waitFor();
                if (retVal != 0) {
                    throw new RuntimeException("could not execute: git mv " + originalFile + " " + newFile + ", return value: " + retVal);
                }

                git.commit()
                        .setAuthor(ident)
                        .setCommitter(ident)
                        .setMessage("move [" + originalFile + "] to [" + newFile + "] " + " (SVN revision " + revision.getNumber() + ")")
                        .call();

                git.checkout().setStartPoint("HEAD~").addPath(originalFile).call();

                git.commit()
                        .setAuthor(ident)
                        .setCommitter(ident)
                        .setMessage("restore [" + originalFile + "] (SVN revision " + revision.getNumber() + ")")
                        .call();

            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void changeExistingFile(Node node) {
        String nodePath = node.getHeaders().get(NodeHeader.PATH);
        if (nodePath == null) {
            throw new RuntimeException("missing path for node change");
        }
        String absolutePath = gitDir.getAbsolutePath() + File.separator + nodePath;
        writeFile(absolutePath, node.getByteContent());

        final Revision revision = node.getRevision().get();
        final PersonIdent ident = getIdent(revision);
        try {
            gitAddAll();
            git.commit()
                    .setAuthor(ident)
                    .setCommitter(ident)
                    .setMessage("change file [" + nodePath + "] (SVN revision " + revision.getNumber() + ")")
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private void writeFile(String absolutePath, byte[] content) {
        File newFile = new File(absolutePath);

        try(FileOutputStream fos = new FileOutputStream(newFile, false)){
            fos.write(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteNode(Node node) {
        String nodePath = node.getHeaders().get(NodeHeader.PATH);
        if (nodePath == null) {
            throw new RuntimeException("missing path for node deletion");
        }

        try {
            Status status = git.status().call();
            if (status.getAdded().contains(nodePath)) {
                git.reset().addPath(nodePath).call();
            }
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        String absolutePath = gitDir.getAbsolutePath() + File.separator + nodePath;

        File toDelete = new File(absolutePath);
        if (!toDelete.exists()) {
            ps().println(String.format("[%5d] Cannot delete non-existent file: %s",
                    node.getRevision().get().getNumber(), nodePath));
            return;
        }

        if (toDelete.isDirectory()) {
            boolean deleted = deleteDirectory(toDelete);
            if (!deleted) {
                throw new RuntimeException("Did not delete directory: " + absolutePath);
            }
        } else {
            boolean deleted = new File(absolutePath).delete();
            if (!deleted) {
                throw new RuntimeException("Did not delete file: " + absolutePath);
            }
        }

        final Revision revision = node.getRevision().get();
        final PersonIdent ident = getIdent(revision);
        try {

            gitAddAll();
            git.commit()
                    .setAuthor(ident)
                    .setCommitter(ident)
                    .setMessage("deleted [" + nodePath + "] (SVN revision " + revision.getNumber() + ")")
                    .call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private void gitAddAll() {
        try {
            Process gitAdd = new ProcessBuilder().command("/usr/bin/git", "add", ".").directory(gitDir).start();

            int retVal = gitAdd.waitFor();
            if (retVal != 0) {
                throw new RuntimeException("could not execute: 'git add .' , return value: " + retVal);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // from https://www.baeldung.com/java-delete-directory
    boolean deleteDirectory(File directoryToBeDeleted) {
        File[] allContents = directoryToBeDeleted.listFiles();
        if (allContents != null) {
            for (File file : allContents) {
                deleteDirectory(file);
            }
        }
        return directoryToBeDeleted.delete();
    }

    private void doCommit(Revision revision) {
        try {
            git.checkout().setName("master").call();
            ps().println(String.format("[%5d] Checked out 'master' branch.", revision.getNumber()));

            final String gitBranchName = gitBranchForSvnRevision.getName();
            ps().print(String.format("[%5d] Executing a merge --squash ...", revision.getNumber()));
            ps().flush();
            Process gitMergeCommand = new ProcessBuilder(
                    "/usr/bin/git", "merge", "--squash", "-q", gitBranchName
            ).directory(this.gitDir).start();
            int retVal = gitMergeCommand.waitFor();
            if (retVal != 0) {
                throw new RuntimeException("could not execute: git merge --squash " + gitBranchName + ", return value: " + retVal);
            }
            ps().println("done.");

            PersonIdent author = getIdent(revision);
            ps().print(String.format("[%5d] Committing the merge commit ... ", revision.getNumber()));
            ps().flush();
            RevCommit revCommit = git.commit()
                    .setMessage(getMessage(revision))
                    .setAuthor(author)
                    .setCommitter(author)
                    .call();
            ps().println("done.");

            git.branchDelete().setForce(true).setBranchNames(gitBranchName).call();
            ps().println(String.format("[%5d] Committed: %s", revision.getNumber(), revCommit.getName()));
            svnRevisionToGitHash
                    .computeIfAbsent(revision.getNumber(), s -> new ArrayList<>())
                    .add(revCommit.getName());
        } catch (GitAPIException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private String getMessage(Revision revision) {
        return revision.get("svn:log") + "\nSVN revision: " + revision.getNumber();
    }

    private PersonIdent getIdent(Revision revision) {
        String date = revision.getProperties().get("svn:date");
        Date parsedDate = null;
        try {
            parsedDate = dateFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        return new PersonIdent("Dan", "dan@example.com", parsedDate, TimeZone.getTimeZone("UTC"));
    }
}