# SVN Dump GUI

Attempt to create a user interface for editing SVN dump files.

## Background

SVN dump files are created via the `svnadmin dump` command, and contain all the 
history of an SVN repository.  An SVN dump file contains a list of revisions 
(see [`SvnRevision`](src/main/java/com/github/cstroe/svndumpgui/api/SvnRevision.java)), and each
revision contains a list of nodes (see [`SvnNode`](src/main/java/com/github/cstroe/svndumpgui/api/SvnNode.java)).

Revisions can have properties such as author, date, and commit message.  Nodes 
can have properties too, which are maintained on a node by node basis.

## SVNDumpFileParser

The `SvnDumpFileParser` is an auto-generated parser for SVN dump files 
(files [created with `svnadmin dump`](src/test/resources/dumps)).  It will
parse SVN dump files into an [`SvnDump`](src/main/java/com/github/cstroe/svndumpgui/api/SvnDump.java) object.  The `SvnDump` representation is
meant to be very light-weight and does minimal validation.

The parser is auto-generated using JavaCC from the [`svndump.jj`](src/main/javacc/svndump.jj) gramar file.
This grammar generates a parser that is dependenent on the Java interfaces and 
classes in this project.

### Usage

Parsing an SVN dump file is straight forward:

    InputStream s = new FileInputStream("svn_dump_file");
    SvnDumpFileParser parser = new SvnDumpFileParser(s, "ISO-8859-1");
    SvnDump dump = parser.Start();

See [`SvnDumpFileParserTest`](src/test/java/com/github/cstroe/svndumpgui/internal/SvnDumpFileParserTest.java) for usage patterns of the parser.

## Svn dump summary

To get an `svn log`-like summary of your dump file, you can use the 
[`SvnDumpSummary`](src/test/java/com/github/cstroe/svndumpgui/internal/SvnDumpSummary.java).

## Mutators

The API allows for changing of an SVN dump file via 
[`SvnDumpMutator`](src/main/java/com/github/cstroe/svndumpgui/api/SvnDumpMutator.java) implementations.

Some useful mutators are:
* [`ClearRevision`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/ClearRevision.java) - empties a revision (removes all changes, revision is preserved)
* [`PathChange`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/PathChange.java) - updates file/dir paths
* [`NodeRemove`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/NodeRemove.java) - removes an individual file change from a revision
* [`NodeAdd`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/NodeAdd.java) - add some newly crafted change to a specific revision
* [`NodeHeaderChange`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/NodeHeaderChange.java) - change a specific property on an existing SvnNode

To apply multiple mutators in sequence, you can add them to a 
[`MutatorChain`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/MutatorChain.java).

## Validators

When you start messing with your SVN history via the mutators, you can be left
with an SVN dump file that cannot be imported back into an SVN repository.  To
 make changing SVN history easier the API has the concept of an 
 [`SvnDumpValidator`](src/main/java/com/github/cstroe/svndumpgui/api/SvnDumpMutator.java).
 
Validation is done while the data is in memory, which is much faster
than running it through `svnadmin load`.

Some useful validators:
* [`PathCollision`](src/main/java/com/github/cstroe/svndumpgui/internal/validate/PathCollision.java) - checks that file operations are valid (don't delete non-existent files, don't double add files, check that files exist when making copies)