[![Build Status](https://travis-ci.org/cstroe/svndumpgui.svg?branch=master)](https://travis-ci.org/cstroe/svndumpgui)

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

## Svn dump summary

To get an `svn log`-like summary of your dump file, you can use the 
[`SvnDumpSummary`](src/main/java/com/github/cstroe/svndumpgui/internal/utility/SvnDumpSummary.java) (sample output [here](src/test/resources/summary/svn_multi_file_delete.txt)).

## Consumers

An `SvnDumpConsumer` consumes the various pieces of an `SvnDump`.  Specializations of a consumer are:

* `SvnDumpMutator`: changes the SvnDump in some way
* `SvnDumpValidator`: validates the correctness of the SvnDump in some way
* `SvnDumpWriter`: write the SvnDump in some format

Consumers (and therefore any of its specializations) can be chained together to achieve complex operations on SVN dump files.

## Mutators

The API allows for changing of an SVN dump file via 
[`SvnDumpMutator`](src/main/java/com/github/cstroe/svndumpgui/api/SvnDumpMutator.java) implementations.

Some useful mutators are:
* [`ClearRevision`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/ClearRevision.java) - empties a revision (removes all changes, revision is preserved)
* [`PathChange`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/PathChange.java) - updates file/dir paths
* [`NodeRemove`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/NodeRemove.java) - removes an individual file change from a revision
* [`NodeAdd`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/NodeAdd.java) - add some newly crafted change to a specific revision
* [`NodeHeaderChange`](src/main/java/com/github/cstroe/svndumpgui/internal/transform/NodeHeaderChange.java) - change a specific property on an existing SvnNode

To apply multiple mutators in sequence, you can chain them together, using `SvnDumpConsumer.continueTo(SvnDumpConsumer)`.

## Validators

When you start messing with your SVN history via the mutators, you can be left
with an SVN dump file that cannot be imported back into an SVN repository.  To
 make changing SVN history easier the API has the concept of an 
 [`SvnDumpValidator`](src/main/java/com/github/cstroe/svndumpgui/api/SvnDumpValidator.java).
 
Validation is done while the data is in memory, which is much faster
than running it through `svnadmin load`.

Some useful validators:
* [`PathCollision`](src/main/java/com/github/cstroe/svndumpgui/internal/validate/PathCollision.java) - checks that file operations are valid (don't delete non-existent files, don't double add files, check that files exist when making copies)

# Usage

## Example: AgreementMaker

To see how all these pieces fit together to allow you to edit SVN history,
you can look at a SVN repository cleanup that I did for the AgreementMaker project.
All the operations to the SVN dump file are detailed in [this test](src/test/java/com/github/cstroe/svndumpgui/internal/AMDump.java).

## Reading an SVN dump file

Parsing an SVN dump file is straight forward:

    ConsumerChain chain = new ConsumerChain();
    SvnDumpWriter writer = new SvnDumpWriterImpl();
    writer.writeTo(System.out);
    chain.add(writer);

    InputStream s = new FileInputStream("svn.dump");
    SvnDumpFileParser parser = new SvnDumpFileParser(s, "ISO-8859-1");
    SvnDump dump = parser.Start(chain);

See [`SvnDumpFileParserTest`](src/test/java/com/github/cstroe/svndumpgui/internal/SvnDumpFileParserTest.java) for usage patterns of the parser.
