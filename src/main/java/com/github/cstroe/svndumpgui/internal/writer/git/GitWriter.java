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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class GitWriter extends AbstractRepositoryWriter {
    private final File gitDir;
    private final Git git;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");

    private Revision currentRevision;
    private Node currentNode;

    private String authorName = "Dan Langille";
    private String authorEmail = "dan@langille.org";

    private Map<Integer, List<String>> svnRevisionToGitHash = new HashMap<>(9182);
    private SvnBranchState branchState = new SvnBranchState();

    private final Integer resumeFromRevision;

    public GitWriter() throws IOException, GitAPIException {
        this(0,
                Files.createTempDirectory("svndumpadmin-git-").toFile().getAbsolutePath());
    }

    public GitWriter(Integer resumeFromRevision, String gitDir) throws IOException, GitAPIException {
        File tmp = new File(gitDir);
        String dirName = tmp.getName();
        this.gitDir = new File("/tmpfs/" + dirName);
        this.resumeFromRevision = resumeFromRevision;
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
    public void endRevision(Revision revision) {
        super.endRevision(revision);
        if (revision.getNumber() == 0 || revision.getNumber() < resumeFromRevision) {
            ps().println(String.format("[%5s] Skipping revision.", revision.getNumber()));
            return; // skip revision
        }

        List<Node> nodes = revision.getNodes();

        if (containsBaseDir(revision)) {
            getTopLevelNodes(revision).stream().forEach(topLevelNode -> {
                SvnBaseDir dir = SvnBaseDir.of(topLevelNode.getPath().get());
                ps().println(String.format("[%5s] Found base dir: %s", revision.getNumber(), dir));
                branchState.addBaseDir(dir);
            });
            return;
        }

        nodes.forEach(this::processNode);
        doCommit(revision);
    }

    private void doCommit(Revision revision) {
        try {
            Status status = git.status().call();
            if (status.isClean()) {
                return;
            }

            git.add().addFilepattern(".").call();
            String date = revision.getProperties().get("svn:date");
            Date parsedDate = dateFormat.parse(date);

            PersonIdent committer = new PersonIdent("Dan", "dan@example.com", parsedDate, TimeZone.getTimeZone("UTC"));

            String message = revision.get("svn:log") + "\nSVN revision: " + revision.getNumber();

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
        } catch (GitAPIException | ParseException e) {
            throw new RuntimeException(e);
        }
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
     * Revision: 1
     *     svn:date: 2000-04-23T04:59:18.000000Z
     *     svn:log: Standard project directories initialized by cvs2svn.
     *     Node
     *         Headers:
     *             Node-path: : www
     *             Node-kind: : dir
     *             Node-action: : add
     *             Prop-content-length: : 10
     *             Content-length: : 10
     *         Properties:
     *             TRAILING_NEWLINE_HINT: 2
     *     Node
     *         Headers:
     *             Node-path: : www/branches
     *             Node-kind: : dir
     *             Node-action: : add
     *             Prop-content-length: : 10
     *             Content-length: : 10
     *         Properties:
     *             TRAILING_NEWLINE_HINT: 2
     *     Node
     *         Headers:
     *             Node-path: : www/tags
     *             Node-kind: : dir
     *             Node-action: : add
     *             Prop-content-length: : 10
     *             Content-length: : 10
     *         Properties:
     *             TRAILING_NEWLINE_HINT: 2
     *     Node
     *         Headers:
     *             Node-path: : www/trunk
     *             Node-kind: : dir
     *             Node-action: : add
     *             Prop-content-length: : 10
     *             Content-length: : 10
     *         Properties:
     *             TRAILING_NEWLINE_HINT: 2
     */
    private boolean containsBaseDir(Revision revision) {
        // has top level directory?
        Optional<Node> maybeTopLevelNode = getTopLevelNodes(revision).stream().findFirst(); // this is bad, we need to track each basedir
        if (!maybeTopLevelNode.isPresent()) {
            return false;
        }

        // nodes exist to match <base>/trunk, <base>/branches, <base>/tags
        Optional<Node> trunkNode = getSuffixNode(revision, maybeTopLevelNode.get(), "trunk");
        Optional<Node> branchesNode = getSuffixNode(revision, maybeTopLevelNode.get(), "branches");
        Optional<Node> tagsNode = getSuffixNode(revision, maybeTopLevelNode.get(), "tags");

        // all nodes are under the top level node
        return trunkNode.isPresent() &&
                branchesNode.isPresent() &&
                tagsNode.isPresent() &&
                revision.getNodes().size() % 4 == 0; // HACK!
    }

    private List<Node> getTopLevelNodes(Revision revision) {
        return revision.getNodes().stream()
                .filter(n -> n.getPath().filter(p -> !p.contains("/")).isPresent())
                .collect(Collectors.toList());
    }

    private Optional<Node> getSuffixNode(Revision revision, Node topLevelNode, String suffix) {
        return topLevelNode.getPath().flatMap(s -> revision.getNodes().stream()
                .filter(n -> n.getPath()
                        .filter(p -> p.equals(s + "/" + suffix))
                        .isPresent())
                .findFirst());
    }

    /*
    Revision: 519
        svn:date: 2001-10-08T02:56:11.000000Z
        svn:log: This commit was manufactured by cvs2svn to create branch 'FreshPorts2'.
        Node
            Headers:
                Node-path: : configuration/branches/FreshPorts2
                Node-kind: : dir
                Node-action: : add
                Node-copyfrom-rev: : 517
                Node-copyfrom-path: : configuration/trunk
            Properties:
                TRAILING_NEWLINE_HINT: 2
    */
    private boolean isBranchCreateNode(Node node) {
        if (!branchState.isInBranch(node)) {
            return false;
        }

        String path = node.getPath().get();
        String[] split = path.split("/");

        final Map<NodeHeader, String> headers = node.getHeaders();
        return node.isDir() && node.isAdd() &&
                split.length == 3 && "branches".equals(split[1]);
    }

    // write a Node file out
    private void processNode(Node node) {
        if (isBranchCreateNode(node)) {
            createBranch(node);
            return;
        }

        if (node.isDir()) {
            ps().println(String.format("[%5s] Skipping directory node: %s", node.getRevision().get().getNumber(), node.getPath().get()));
            return; // git doesn't track directories
        }

        if (node.isFile()) {
            if (branchState.isInBranch(node)) {
                try {
                    SvnBranch branch = branchState.getBranch(node).get();

                    if (!branch.equals(git.getRepository().getBranch())) {
                        Status status = git.status().call();
                        if (!status.isClean()) {
                            doCommit(node.getRevision().get());
                        }

                        Ref ref = git.checkout().setName(branch.getGitName()).call();
                    }
                } catch (GitAPIException | IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    if (!"master".equals(git.getRepository().getBranch())) {
                        git.checkout().setName("master").call();
                    }
                } catch (GitAPIException | IOException e) {
                    throw new RuntimeException(e);
                }
            }

            if (branchState.isInTags(node)) {
                ps().println(String.format("[%5s] Skipping tags node: %s", node.getRevision().get().getNumber(), node.getPath().get()));
                return; // ignore tags for now
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
                default:
                    throw new RuntimeException("Can't handle node action: " + maybeAction.get());
            }
        }

        if (node.isDelete()) {
            if (branchState.isInTags(node)) {
                ps().println(String.format("[%5s] Skipping tags node: %s", node.getRevision().get().getNumber(), node.getPath().get()));
            } else {
                deleteNode(node);
            }
            return;
        }

        throw new RuntimeException("Unhandled node:\n" + node);
    }

    private void createBranch(Node node) {
        SvnBaseDir baseDir = branchState.getBranchDir(node).get();
        String branchName = baseDir.stripBranchPrefix(node.getPath().get()).get();
        List<Ref> refs = null;
        try {
            refs = git.branchList().call();
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
        try {
            if (refs.stream().noneMatch(r -> ("refs/heads/" + branchName).equals(r.getName()))) {
                Ref ref = git.branchCreate()
                        .setName(branchName)
                        .call();
                ps().println(String.format("[%5s] Created branch: %s", node.getRevision().get().getNumber(), ref.getName()));
            }
            branchState.addBranch(baseDir, SvnBranch.of(branchName, branchName));

            // check out the branch
            git.checkout().setName(branchName).call();
            ps().println(String.format("[%5s] Checked out branch: %s", node.getRevision().get().getNumber(), branchName));

            String copyFromPath = node.get(NodeHeader.COPY_FROM_PATH);
            if (copyFromPath != null) {
                String cleanCopyFromPath = cleanPath(copyFromPath);
                Integer copyFromRevision = Integer.valueOf(node.get(NodeHeader.COPY_FROM_REV));
                List<String> gitShas = svnRevisionToGitHash.get(copyFromRevision);
                if (gitShas.size() != 1) {
                    throw new RuntimeException("Not the correct number of git shas: " + gitShas.size());
                }

                String copyFromGitSha = gitShas.get(0);

                git.checkout()
                        .setStartPoint(copyFromGitSha)
                        .addPath(cleanCopyFromPath)
                        .call();
            }
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }
    }

    private void addNewFile(Node node) {
        String nodePath = node.getHeaders().get(NodeHeader.PATH);
        if (nodePath == null) {
            throw new RuntimeException("missing path for node add");
        }

        String cleanPath = cleanPath(nodePath);

        String absolutePath = gitDir.getAbsolutePath() + File.separator + cleanPath;

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
        String cleanPath = cleanPath(nodePath);
        String absolutePath = gitDir.getAbsolutePath() + File.separator + cleanPath;

        File newFile = new File(absolutePath);

        try(FileOutputStream fos = new FileOutputStream(newFile, false)){
            fos.write(node.getByteContent());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void deleteNode(Node node) {
        String nodePath = node.getHeaders().get(NodeHeader.PATH);
        if (nodePath == null) {
            throw new RuntimeException("missing path for node deletion");
        }
        String cleanNodePath = cleanPath(nodePath);

        try {
            Status status = git.status().call();
             if (status.getAdded().contains(cleanNodePath)) {
                 git.reset().addPath(cleanNodePath).call();
             }
        } catch (GitAPIException e) {
            throw new RuntimeException(e);
        }

        String absolutePath = gitDir.getAbsolutePath() + File.separator + cleanNodePath;

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

//    private Revision printRevisionInfo(@NotNull Revision revision) {
//        System.err.println("Revision: " + revision.getNumber());
//        revision.getProperties().entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .forEachOrdered(es -> System.err.println("    " + es.getKey() + ": " + es.getValue()));
//        return revision;
//    }
//
//    private Node printNodeInfo(@NotNull Node node) {
//        System.err.println("    Node");
//        System.err.println("        Headers:");
//        node.getHeaders().entrySet().stream()
//                .sorted(Map.Entry.comparingByKey())
//                .forEachOrdered(es -> System.err.println("            " + es.getKey() + ": " + es.getValue()));
////        System.err.println("        Properties:");
////        node.getProperties().entrySet().stream()
////                .sorted(Map.Entry.comparingByKey())
////                .forEachOrdered(es -> System.err.println("            " + es.getKey() + ": " + es.getValue()));
//        return node;
//    }

    @Override
    public void writeTo(OutputStream os) {}

    public static String cleanPath(String path) {
        String[] splits = path.split("/");
        if ("trunk".equals(splits[1])) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < splits.length; i++) {
                if (i == 1) { continue; }
                if (i != 0) { builder.append("/"); }
                builder.append(splits[i]);
            }
            return builder.toString();
        } else if ("branches".equals(splits[1])) {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < splits.length; i++) {
                if (i == 1 || i == 2) { continue; }
                if (i != 0) { builder.append("/"); }
                builder.append(splits[i]);
            }
            return builder.toString();
        } else {
            throw new RuntimeException("unknown path: " + path);
        }
    }
}
