package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.MergeResult;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A Git writer that treats all SVN paths as files.  No branches, no tags.
 */
public class GitWriterNoBranching extends AbstractRepositoryWriter {
    private final File gitDir;
    private final Git git;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");

    private Revision currentRevision;
    private Node currentNode;
    private Map<Integer, List<String>> svnRevisionToGitHash = new HashMap<>(9182);

    public GitWriterNoBranching() throws IOException, GitAPIException {
        this(Files.createTempDirectory("svndumpadmin-git-").toFile().getAbsolutePath());
    }

    public GitWriterNoBranching(String gitDir) throws GitAPIException {
        File tmp = new File(gitDir);
        String dirName = tmp.getName();
        this.gitDir = new File("/tmpfs/" + dirName);
        System.out.println("Git directory: " + this.gitDir.getAbsolutePath());
        git = Git.init()
                .setDirectory(this.gitDir)
                .call();
    }

    @Override
    public void consume(Revision revision) {
        super.consume(revision);
        currentRevision = revision;
    }

    @Override
    public void consume(Node node) {
        super.consume(node);
        currentNode = node;
    }

    @Override
    public void endNode(Node node) {
        super.endNode(node);
        currentRevision.addNode(node);
        node.setRevision(currentRevision);
    }

    @Override
    public void consume(ContentChunk chunk) {
        super.consume(chunk);
        currentNode.addFileContentChunk(chunk);
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
        if (revision.getNumber() == 0) {
            ps().println(String.format("[%5s] Skipping revision.", revision.getNumber()));
            return; // skip revision
        }

        List<Node> nodes = revision.getNodes();
        nodes.forEach(this::processNode);
        doCommit(revision);
    }

    private void processNode(Node node) {
        if (node.isDir() && node.getHeaders().get(NodeHeader.COPY_FROM_REV) == null) {
            ps().println(String.format("[%5s] Skipping empty directory node: %s", node.getRevision().get().getNumber(), node.getPath().get()));
            return;
        } else if (node.isDir()) {
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
                ps().println(String.format("[%5d] Restoring directory from: %s:%s at %s",
                        node.getRevision().get().getNumber(), copyFromPath, copyFromRev, copyFromGitSha));

                String gitBranchName = "rev-" + node.getRevision().get().getNumber();
                Ref gitBranchCreated = git.checkout().setName(gitBranchName).setCreateBranch(true).call();


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

                for (List<String> file : result) {
                    String originalFile = file.get(3);
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

                }

                git.checkout().setName("master").call();

                Process gitMvCommand = new ProcessBuilder(
                        "/usr/bin/git", "merge", "--squash", gitBranchName
                ).directory(this.gitDir).start();
                int retVal = gitMvCommand.waitFor();
                if (retVal != 0) {
                    throw new RuntimeException("could not execute: git merge --squash " + gitBranchName + ", return value: " + retVal);
                }

                PersonIdent author = getIdent(node.getRevision().get());
                git.commit()
                        .setMessage(getMessage(node.getRevision().get()))
                        .setAuthor(author)
                        .setCommitter(author)
                        .call();

                git.branchDelete().setForce(true).setBranchNames(gitBranchName).call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

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
            default:
                throw new RuntimeException("Can't handle node action: " + maybeAction.get());
        }
    }

    private void addNewFile(Node node) {
        if (node.getHeaders().get(NodeHeader.COPY_FROM_REV) != null) {
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
                ps().println(String.format("[%5d] Restoring directory from: %s:%s at %s",
                        node.getRevision().get().getNumber(), copyFromPath, copyFromRev, copyFromGitSha));

                String gitBranchName = "rev-" + node.getRevision().get().getNumber();
                Ref gitBranchCreated = git.checkout().setName(gitBranchName).setCreateBranch(true).call();


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

                for (List<String> file : result) {
                    String originalFile = file.get(3);
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

                }

                git.checkout().setName("master").call();

                Process gitMvCommand = new ProcessBuilder(
                        "/usr/bin/git", "merge", "--squash", gitBranchName
                ).directory(this.gitDir).start();
                int retVal = gitMvCommand.waitFor();
                if (retVal != 0) {
                    throw new RuntimeException("could not execute: git merge --squash " + gitBranchName + ", return value: " + retVal);
                }

                PersonIdent author = getIdent(node.getRevision().get());
                git.commit()
                        .setMessage(getMessage(node.getRevision().get()))
                        .setAuthor(author)
                        .setCommitter(author)
                        .call();

                git.branchDelete().setForce(true).setBranchNames(gitBranchName).call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

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
    }

    private void changeExistingFile(Node node) {
        String nodePath = node.getHeaders().get(NodeHeader.PATH);
        if (nodePath == null) {
            throw new RuntimeException("missing path for node change");
        }
        String absolutePath = gitDir.getAbsolutePath() + File.separator + nodePath;
        writeFile(absolutePath, node.getByteContent());
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
            throw new RuntimeException("Cannot delete missing file: " + absolutePath);
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
            Status status = git.status().call();
            if (status.isClean()) {
                return;
            }

            git.add().addFilepattern(".").call();
            PersonIdent committer = getIdent(revision);
            String message = getMessage(revision);
            RevCommit revCommit = git.commit()
                    .setAll(true)
                    .setAuthor(committer)
                    .setCommitter(committer)
                    .setMessage(message)
                    .call();
            ps().println(String.format("[%5s] Committed: %s", revision.getNumber(), revCommit.getName()));
            svnRevisionToGitHash
                    .computeIfAbsent(revision.getNumber(), s -> new ArrayList<>())
                    .add(revCommit.getName());
        } catch (GitAPIException e) {
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