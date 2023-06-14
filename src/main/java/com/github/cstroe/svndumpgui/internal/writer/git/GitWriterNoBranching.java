package com.github.cstroe.svndumpgui.internal.writer.git;

import com.github.cstroe.svndumpgui.api.ContentChunk;
import com.github.cstroe.svndumpgui.api.Node;
import com.github.cstroe.svndumpgui.api.NodeHeader;
import com.github.cstroe.svndumpgui.api.Revision;
import com.github.cstroe.svndumpgui.internal.utility.Pair;
import com.github.cstroe.svndumpgui.internal.utility.Tuple2;
import com.github.cstroe.svndumpgui.internal.writer.AbstractRepositoryWriter;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.javatuples.Tuple;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.String.join;

/**
 * A Git writer that treats all SVN paths as files.  No branches, no tags.
 */
public class GitWriterNoBranching extends AbstractRepositoryWriter {
    private final File gitDir;
    private final String mainBranch;
    private final Git git;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
    private final int startFromRev;

    private Revision currentRevision;
    private Node currentNode;
    private Ref gitBranchForSvnRevision;
    //private Map<Integer, List<String>> svnRevisionToGitHash = new HashMap<>(9182);
    private Map<String, List<Tuple2<Integer, String>>> branchToRevisionToSha = new HashMap<>(9182);

    public GitWriterNoBranching(String gitDir) throws IOException, GitAPIException {
        this(gitDir, "master");
    }

    public GitWriterNoBranching(String gitDir, String mainBranch) throws IOException, GitAPIException {
        this(gitDir, mainBranch, 0);
    }

    public GitWriterNoBranching(String gitDir, String mainBranch, int startFromRev) throws GitAPIException, IOException {
        this.gitDir = new File(gitDir);
        this.startFromRev = startFromRev;
        this.mainBranch = mainBranch;

        if (!this.gitDir.exists()) {
            throw new RuntimeException("Directory does not exist: " + this.gitDir.getAbsolutePath());
        }

        this.git = Git.init()
                .setDirectory(this.gitDir)
                .call();
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
            createInitialCommit(revision);
            return;
        }

        List<Node> revisionNodes = revision.getNodes().stream().filter(node -> {
            boolean isDirCreate = node.isDir() && node.getHeaders().get(NodeHeader.COPY_FROM_REV) == null;
            if (isDirCreate) {
                ps().println(String.format("[%5s] Skipping empty directory node: %s", node.getRevision().get().getNumber(), node.getPath().get()));
            }
            return !isDirCreate;
        }).collect(Collectors.toList());
        if (revisionNodes.isEmpty()) {
            ps().println(String.format("[%5d] Skipping empty revision.", revision.getNumber()));
            return;
        }

        Map<String, List<Pair<Node, String>>> nodesByBranch = separateNodesByBranch(revisionNodes);
        for (Map.Entry<String, List<Pair<Node, String>>> entry : nodesByBranch.entrySet()) {
            String parentBranch = entry.getKey();
            try {
                if (!parentBranch.equals(git.getRepository().getBranch())) {
                    git.checkout().setName(parentBranch).call();
                }
                ps().println(String.format("[%5d] Checked out branch: %s", revision.getNumber(), parentBranch));
            } catch (GitAPIException | IOException e) {
                throw new RuntimeException(e);
            }

            // start a new branch
            String workingBranch = parentBranch + "-rev-" + revision.getNumber();
            try {
                Ref gitBranchCreated = git.checkout().setName(workingBranch).setCreateBranch(true).call();
                this.gitBranchForSvnRevision = gitBranchCreated;
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            ps().println(String.format("[%5d] Created new branch %s.", revision.getNumber(), workingBranch));

            boolean changed = false;
            for (Pair<Node, String> nodeWithPath : entry.getValue()) {
                boolean hasChanged = processNode(nodeWithPath.first, parentBranch, workingBranch, nodeWithPath.second);
                changed = changed || hasChanged;
            }

            doCommit(revision, parentBranch, workingBranch, changed);
        }
    }

    private Map<String, List<Pair<Node, String>>> separateNodesByBranch(List<Node> nodes) {
        Map<String, List<Pair<Node, String>>> nodesByBranch = new HashMap<>();

        for(Node node : nodes) {
            String nodePath = node.getPath().get();
            Pair<String, String> branchInfo = removeBranchPrefix(nodePath);
            nodesByBranch.computeIfAbsent(branchInfo.first, s -> new ArrayList<>())
                    .add(Pair.of(node, branchInfo.second));
        }

        return nodesByBranch;
    }

    private void createInitialCommit(Revision revision) {
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
            RevCommit revCommit = git.commit()
                    .setAuthor(ident)
                    .setCommitter(ident)
                    .setMessage("Initial commit.\nSVN revision: " + revision.getNumber())
                    .call();
            branchToRevisionToSha.computeIfAbsent(this.mainBranch, s -> new ArrayList<>())
                            .add(Tuple2.of(revision.getNumber(), revCommit.getName()));
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        ps().println(String.format("[%5s] Created an initial commit.", revision.getNumber()));
    }

    private static Pattern branchPattern = Pattern.compile("^branches/([a-zA-Z0-9]+)$", Pattern.MULTILINE);
    private static Pattern isInBranchPattern = Pattern.compile("^branches/([a-zA-Z0-9]+)/(.+)$", Pattern.MULTILINE);

    /**
     * @return true if a change was committed
     */
    private boolean processNode(Node node, String parentBranch, String workingBranch, String path) {
        if (node.isDir() && node.getHeaders().get(NodeHeader.COPY_FROM_REV) == null) {
            throw new RuntimeException("node should have been filtered out:\n" + node);
        } else if (node.isDir() && branchPattern.matcher(node.getPath().get()).matches()) {
            try {
                String currentBranch = git.getRepository().getBranch();
                createBranch(node);
                // checkout previous branch
                if (!currentBranch.equals(git.getRepository().getBranch())) {
                    git.checkout().setName(currentBranch).call();
                }
            } catch (GitAPIException | IOException e) {
                throw new RuntimeException(e);
            }
            return false;
        } else if (node.isDir()) {
            createDirectoryFromHistory(node, path);
            return true;
        }

        // write the file
        Optional<String> maybeAction = node.getAction();
        if (!maybeAction.isPresent()) {
            return false;
        }
        switch (maybeAction.get()) {
            case "add":
                addNewFile(node, path);
                return true;
            case "change":
                changeExistingFile(node, path);
                return true;
            case "delete":
                deleteNode(node, path);
                return true;
            case "replace":
                replaceNode(node, path);
                return true;
            default:
                throw new RuntimeException("Can't handle node action: " + maybeAction.get());
        }
    }

    private void createBranch(Node node) {
        Matcher branchMatcher = branchPattern.matcher(node.getPath().get());
        if (!branchMatcher.find()) {
            throw new RuntimeException("createBranch called with non-branch node");
        }

        String branchName = branchMatcher.group(1);

        try {
            int revision = Integer.valueOf(node.getHeaders().get(NodeHeader.COPY_FROM_REV));
            Tuple2<Integer, String> sha = findGitSha(this.mainBranch, revision);
            if (sha == null) {
                throw new RuntimeException("Could not find sha for revision " + revision);
            }

            Ref ref = git.branchCreate()
                    .setStartPoint(sha._2)
                    .setName(branchName)
                    .call();

            ps().println(String.format("[%5s] Created branch '%s' from %s", node.getRevision().get().getNumber(), ref.getName(), sha));
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private Tuple2<Integer, String> findGitSha(String branch, int revision) {
        List<Tuple2<Integer, String>> shas = branchToRevisionToSha.get(branch);
        if (shas == null) {
            throw new RuntimeException("branch does not exist: " + branch);
        }

        if (shas.size() < 1) {
            throw new RuntimeException("could not find at least one sha for branch: " + branch);
        }

        Tuple2<Integer, String> currentSha = shas.get(0);
        for (int i = 1; i < shas.size(); i++) {
            Tuple2<Integer, String> nextSha = shas.get(i);
            if (nextSha._1 > revision) {
                break;
            }
            currentSha = nextSha;
        }
        return currentSha;
    }

    private void replaceNode(Node node, String nodePath) {
        changeExistingFile(node, nodePath);
    }

    private void createDirectoryFromHistory(Node node, String nodePath) {
        String copyFromRev = node.getHeaders().get(NodeHeader.COPY_FROM_REV);
        String copyFromPathRaw = node.getHeaders().get(NodeHeader.COPY_FROM_PATH);
        if (copyFromRev == null || copyFromPathRaw == null) {
            throw new RuntimeException("A revision is missing a path");
        }

        Pair<String, String> copyFromPath = removeBranchPrefix(copyFromPathRaw);
        final String sourceBranch = copyFromPath.first;

        Integer copyFromRevision = Integer.valueOf(node.get(NodeHeader.COPY_FROM_REV));

        Tuple2<Integer, String> copyFromGitSha = findGitSha(sourceBranch, copyFromRevision);

        try {
            ps().println(String.format("[%5d] Restoring directory '%s' from: %s:%s at %s",
                    node.getRevision().get().getNumber(), nodePath, copyFromPath, copyFromRev, copyFromGitSha));

            String[] gitCommand = {"/usr/bin/git", "ls-tree", "-r", copyFromGitSha._2, copyFromPath.second};
            ps().println(String.format("[%5d] Executing '%s'", node.getRevision().get().getNumber(), String.join(" ", gitCommand)));
            Process gitLsTree = new ProcessBuilder(gitCommand)
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

            ps().println(String.format("[%5d] Found %d files.",
                    node.getRevision().get().getNumber(), result.size()));
            Iterator<List<String>> resultIter = result.listIterator();

            final Revision revision = node.getRevision().get();
            final PersonIdent ident = getIdent(revision);
            for (int i = 0; i < result.size(); i++) {
                List<String> fileInfo = resultIter.next();
                String originalFile = fileInfo.get(3);
                String newFile = nodePath + File.separator + originalFile.substring(copyFromPath.second.length() + 1);
                File absoluteNewFile = new File(gitDir.getAbsolutePath() + File.separator + newFile);

                if (!absoluteNewFile.exists()) {
                    String[] checkoutCommand = {"/usr/bin/git", "checkout", copyFromGitSha._2, "--", originalFile};
                    ps().println(String.format("[%5d] Executing '%s'", node.getRevision().get().getNumber(), String.join(" ", checkoutCommand)));
                    Process gitCheckout = new ProcessBuilder()
                            .command(checkoutCommand)
                            .directory(gitDir)
                            .start();

                    int gitCheckoutRetVal = gitCheckout.waitFor();
                    if (gitCheckoutRetVal != 0) {
                        throw new RuntimeException("could not execute: '" + String.join(" ", checkoutCommand) + "', return value: " + gitCheckoutRetVal);
                    }

                    Status st = git.status().call();
                    if (!st.isClean()) {
                        git.commit()
                                .setAuthor(ident)
                                .setCommitter(ident)
                                .setMessage("restore [" + originalFile + "] into ["+ newFile +"] (SVN revision " + node.getRevision().get().getNumber() + ")")
                                .call();
                    }

                    File parentDir = absoluteNewFile.getParentFile();
                    if (!parentDir.exists() && !parentDir.mkdirs()) {
                        throw new RuntimeException("could not create directory: " + parentDir.getAbsolutePath());
                    }


                    if (!originalFile.equals(newFile)) {
                        String[] mvCommand = {"/usr/bin/git", "mv", originalFile, newFile};
                        ps().println(String.format("[%5d] Executing '%s'", node.getRevision().get().getNumber(), String.join(" ", mvCommand)));
                        Process gitMvCommand = new ProcessBuilder(mvCommand)
                                .directory(this.gitDir).start();
                        int retVal = gitMvCommand.waitFor();
                        if (retVal != 0) {
                            throw new RuntimeException("could not execute: '" + String.join(" ", mvCommand) + "', return value: " + retVal);
                        }

                        git.commit()
                                .setAuthor(ident)
                                .setCommitter(ident)
                                .setMessage("move [" + originalFile + "] to [" + nodePath + "] " + " (SVN revision " + revision.getNumber() + ")")
                                .call();


                        // restore moved file
                        git.checkout().setStartPoint("HEAD~").addPath(originalFile).call();

                        git.commit()
                                .setAuthor(ident)
                                .setCommitter(ident)
                                .setMessage("restore [" + originalFile + "] (SVN revision " + revision.getNumber() + ")")
                                .call();
                    }
                }



                if (i % 50 == 0) {
                    ps().println(String.format("[%5d] Processing file %d of %d.",
                            node.getRevision().get().getNumber(), i + 1, result.size()));
                }
            }
            ps().println(String.format("[%5d] Processing file %d of %d.",
                    node.getRevision().get().getNumber(), result.size(), result.size()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ps().println(String.format("[%5d] Finished processing files.", node.getRevision().get().getNumber()));
    }

    private void addNewFile(Node node, String nodePath) {
        if (node.getHeaders().get(NodeHeader.COPY_FROM_REV) != null) {
            addNewFileFromHistory(node, nodePath);
            return;
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

    private void addNewFileFromHistory(Node node, String nodePath) {
        String copyFromRev = node.getHeaders().get(NodeHeader.COPY_FROM_REV);
        String copyFromPathRaw = node.getHeaders().get(NodeHeader.COPY_FROM_PATH);

        if (copyFromRev == null || copyFromPathRaw == null) {
            throw new RuntimeException("A revision is missing a path");
        }

        Pair<String, String> copyFromPath = removeBranchPrefix(copyFromPathRaw);
        Integer copyFromRevision = Integer.valueOf(node.get(NodeHeader.COPY_FROM_REV));
        Tuple2<Integer, String> copyFromGitSha = findGitSha(copyFromPath.first, copyFromRevision);

        try {
            ps().println(String.format("[%5d] Restoring file %s@%s from %s",
                    node.getRevision().get().getNumber(), copyFromPath, copyFromRev, copyFromGitSha));

            String[] gitCommand = {"/usr/bin/git", "ls-tree", "-r", copyFromGitSha._2, copyFromPath.second};
            ps().println(String.format("[%5d] Executing '%s'", node.getRevision().get().getNumber(), String.join(" ", gitCommand)));

            Process gitLsTree = new ProcessBuilder(gitCommand)
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
                Pair<String, String> branchPath = removeBranchPrefix(node.getPath().get());

                File absoluteNewFile = new File(gitDir.getAbsolutePath() + File.separator + branchPath.second);
                String[] checkoutCommand = {"/usr/bin/git", "checkout", copyFromGitSha._2, "--", copyFromPath.second};
                ps().println(String.format("[%5d] Executing '%s'", node.getRevision().get().getNumber(), String.join(" ", checkoutCommand)));
                Process gitCheckout = new ProcessBuilder()
                        .command(checkoutCommand)
                        .directory(gitDir)
                        .start();

                int gitCheckoutRetVal = gitCheckout.waitFor();
                if (gitCheckoutRetVal != 0) {
                    throw new RuntimeException("could not execute: '" + String.join(" ", checkoutCommand) + "', return value: " + gitCheckoutRetVal);
                }

                Status st = git.status().call();
                if (!st.isClean()) {
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

                String[] mvCommand = {"/usr/bin/git", "mv", "-f", originalFile, branchPath.second};
                ps().println(String.format("[%5d] Executing '%s'", node.getRevision().get().getNumber(), String.join(" ", mvCommand)));
                Process gitMvCommand = new ProcessBuilder(mvCommand).directory(this.gitDir).start();
                int retVal = gitMvCommand.waitFor();
                if (retVal != 0) {
                    throw new RuntimeException("could not execute: '"+ String.join(" ", mvCommand) +"', return value: " + retVal);
                }

                git.commit()
                        .setAuthor(ident)
                        .setCommitter(ident)
                        .setMessage("move [" + originalFile + "] to [" + branchPath.second + "] " + " (SVN revision " + revision.getNumber() + ")")
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

    private Pair<String, String> removeBranchPrefix(final String nodePath) {
        // remove branch path
        Matcher m = isInBranchPattern.matcher(nodePath);
        if (m.matches()) {
            String branch = m.group(1);
            String path = m.group(2);
            return Pair.of(branch, path);
        }
        return Pair.of(this.mainBranch, nodePath);
    }

    private void changeExistingFile(Node node, String nodePath) {
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

    private void deleteNode(Node node, String nodePath) {
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

    private void doCommit(Revision revision, String parentBranch, String workingBranch, boolean changed) {
        if (!changed) {
            try {
                git.checkout().setName(parentBranch).call();
                git.branchDelete().setBranchNames(workingBranch).call();
                ps().println(String.format("[%5d] Deleted empty branch '%s', back to '%s'.", revision.getNumber(), workingBranch, parentBranch));
                ps().println(String.format("[%5d] Did not change anything in this revision.", revision.getNumber()));
            } catch (GitAPIException e) {
                throw new RuntimeException(e);
            }
            return;
        }

        try {
            git.checkout().setName(parentBranch).call();
            ps().println(String.format("[%5d] Checked out '%s' branch.", revision.getNumber(), parentBranch));

            String[] gitCommand = {"/usr/bin/git", "merge", "--squash", "-q", workingBranch};
            ps().print(String.format("[%5d] Executing '%s' ...", revision.getNumber(), String.join(" ", gitCommand)));
            ps().flush();
            Process gitMergeCommand = new ProcessBuilder(gitCommand).directory(this.gitDir).start();
            int retVal = gitMergeCommand.waitFor();
            if (retVal != 0) {
                throw new RuntimeException(String.format(
                        "could not execute: '%s', return value: %d",
                        String.join(" ", gitCommand), retVal));
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

            git.branchDelete().setForce(true).setBranchNames(workingBranch).call();
            ps().println(String.format("[%5d] Committed: %s", revision.getNumber(), revCommit.getName()));

            branchToRevisionToSha.computeIfAbsent(parentBranch, s -> new ArrayList<>())
                    .add(Tuple2.of(revision.getNumber(), revCommit.getName()));

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

        return new PersonIdent("Dan", "dan@langille.org", parsedDate, TimeZone.getTimeZone("UTC"));
    }
}