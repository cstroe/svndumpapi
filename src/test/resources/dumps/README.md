# How to create the test SVN Dump Files

## PREAMBLE

Most of these commits use the same <SETUP CODE>, shown below:

    mkdir -p svn-test/repos/testrepo
    mkdir -p svn-test/checkout
    cd svn-test/repos
    svnadmin create testrepo
    cd ../checkout
    bash -c 'svn checkout "file:///$(dirname $(pwd))/repos/testrepo" testrepo'
    cd testrepo
    
They also use the same <EXPORT CODE>, shown below:

    cd ../../repos
    svnadmin dump testrepo > output.dump

Replace `output.dump` with the name of the dump file you're creating.

## empty.dump

    mkdir -p svn-test/repos/testrepo
    cd svn-test/repos
    svnadmin create testrepo
    svnadmin dump testrepo > empty.dump
    
## firstcommit.dump

    <SETUP CODE>
    touch firstFile.txt
    svn add firstFile.txt
    svn commit -m "Added a first file."
    <EXPORT CODE>

## add_file.dump

    <SETUP CODE>
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Committed README.txt"
    <EXPORT CODE>

## add_file_no_node_properties.dump

Same as `add_file.dump` but without properties on the node.

## different_node_order.dump

This file was created from a bigger dump of the AgreementMaker SVN repo.
The main difference here is that the order of the "Node-kind" and "Node-path"
are different than `add_file.dump` ("Node-kind" is before "Node-path").

## different_node_order2.dump

This is a hand-hack of `different_node_order.dump` to add `Text-content-sha1` before all
of the other node headers.  Moral of the story, the order of the headers should not matter.

## binary_commit.dump

    <SETUP CODE>
    dd if=/dev/urandom of=file.bin bs=1K count=12
    svn add file.bin
    svn commit -m "Adding binary file."
    <EXPORT CODE>

## svn_rename.dump

    <SETUP CODE>
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Committed README.txt"
    svn mv README.txt README-new.txt
    svn commit -m "Renamed README.txt to README-new.txt"
    <EXPORT CODE>

