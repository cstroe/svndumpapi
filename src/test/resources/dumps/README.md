# How to create the test SVN Dump Files

The `SVNDumpFileParser` unit tests are based on the files in this directory.

## PREAMBLE

### SETUP CODE

Most of these commits use the same `SETUP CODE`, shown below:

    mkdir -p svn-test/repos/testrepo
    mkdir -p svn-test/checkout
    cd svn-test/repos
    svnadmin create testrepo
    cd ../checkout
    bash -c 'svn checkout "file:///$(dirname $(pwd))/repos/testrepo" testrepo'
    cd testrepo

### EXPORT CODE

They also use the same `EXPORT CODE`, shown below:

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

## svn_rename_no_copy_hashes.dump

Same as `svn_rename.dump` but without the copy hashes.

## svn_add_directory.dump

    <SETUP CODE>
    svn mkdir testdir
    svn commit -m "Added a test directory."
    cd testdir
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Added a sample file."
    cd ..
    <EXPORT CODE>

## svn_delete_file.dump

    <SETUP CODE>
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Added a sample file."
    svn delete README.txt
    echo "this is a test file again" > README.txt
    svn add README.txt
    svn commit -m "Added it again."
    <EXPORT CODE>
    
## svn_delete_with_add.dump

    <SETUP CODE>
    svn mkdir testdir
    svn commit -m "Added a directory."
    echo "this is a test file" > README.txt
    svn rm testdir
    svn add README.txt
    svn commit -m "Added and deleted."
    <EXPORT CODE>
    
## svn_multi_dir_delete.dump

    <SETUP CODE>
    svn mkdir testdir1
    svn mkdir testdir2
    svn mkdir testdir3
    svn commit -m "Added 3 dirs."
    svn rm testdir1
    svn rm testdir2
    svn rm testdir3
    svn commit -m "Deleted 3 dirs."
    
## svn_multi_file_delete.dump

    <SETUP CODE>
    echo "this is a test file" > README.txt
    echo "this is a test file" > README2.txt
    echo "this is a test file" > README3.txt
    svn add README.txt README2.txt README3.txt 
    svn commit -m "Added 3 files."
    svn rm README.txt README2.txt README3.txt 
    svn commit -m "Deleted 3 files."
    <EXPORT CODE>
    
##  svn_copy_file.dump

    <SETUP CODE>
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Added readme."
    svn cp README.txt OTHER.txt
    svn commit -m "Copied readme."
    <EXPORT CODE>

## inner_dir.dump

    <SETUP CODE>
    mkdir -p test/innerdir
    touch test/file1.txt test/file2.txt test/innerdir/file3.txt
    svn add test
    svn commit -m "Initial commit."
    svn mv test test-renamed
    svn commit -m "Changed dir name."
    svn rm test-renamed/innerdir/file3.txt
    svn commit -m "Go away inner file."
    <EXPORT CODE>
    
## add_edit_delete_add.dump

    <SETUP CODE>
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Initial commit."
    echo "this is a test file edited" > README.txt
    svn commit -m "Edited file."
    svn rm README.txt
    svn commit -m "Deleted file."
    echo "this is a test file second try" > README.txt
    svn add README.txt
    svn commit -m "Added again."
    <EXPORT CODE>
    