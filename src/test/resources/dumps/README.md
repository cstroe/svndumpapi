# How to create the test SVN Dump Files

Many unit tests are based on the files in this directory.

The `Makefile` calls scripts to create svn dump files.  This is useful in order to understand how the files were created.
If a script does not exist, then the commands to create that dump should be listed below in this file.

Slowly, we should convert all of the instructions below into scripts.

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
    
## composite_commit.dump

    <SETUP CODE>
    mkdir -p d1/d2
    echo "test" > d1/d2/readme2.txt
    svn add d1
    svn commit -m "Initial commit."
    mkdir -p d1/d2/d3/d4
    echo "test" > d1/d2/d3/d4/readme4.txt
    svn add d1/d2/d3
    svn commit -m "Additional sub directory."
    svn cp d1@1 d1-copy
    svn cp d1/d2/d3@2 d1-copy/d2
    svn commit -m "Composite commit."
    <EXPORT CODE>
    
## undelete.dump

    <SETUP CODE>
    touch file1.txt
    svn add file1.txt
    svn commit -m "Initial commit."
    svn rm file1.txt
    svn commit -m "Deleted file1."
    bash -c 'svn cp file:///$(dirname $(dirname $(pwd)))/repos/testrepo/file1.txt@1 file3.txt'
    svn commit -m "Brought it back."
    <EXPORT CODE>
    
## undelete.invalid

A variation on `undelete.dump` but instead of undeleting the file 
from revision 1 we undelete it from revision 2, where it doesn't exist.
It's an invalid svn dump, meant to verify that the validator throws an error.

## simple_branch_and_merge_renamed.dump

    <SETUP CODE>
    mkdir -p trunk/renameddir branches
    echo "this is a test file" > trunk/renameddir/README.txt
    svn add trunk branches
    svn commit -m "Initial commit."
    svn copy trunk branches/mybranch
    svn commit -m "Creating branch."
    cd ..
    bash -c 'svn checkout "file:///$(dirname $(pwd))/repos/testrepo/branches/mybranch" mybranch'
    cd mybranch
    echo "branch work" >> renameddir/README.txt
    svn commit -m "Branch work."
    cd ..
    bash -c 'svn checkout "file:///$(dirname $(pwd))/repos/testrepo/trunk" trunk'
    cd trunk
    bash -c 'svn merge --reintegrate ^/branches/mybranch'
    svn commit -m "Merge branch back into trunk."
    bash -c  'svn delete ^/branches/mybranch -m "Removing branch."'
    <EXPORT CODE>

## property_change_on_file.dump

    <SETUP CODE>
    echo "test file" >> test.txt
    svn add test.txt
    svn commit -m "Adding test file."
    svn ps someproperty value test.txt
    svn commit -m "Setting property."
    svn rm test.txt
    svn commit -m "Delete file."
    <EXPORT CODE>

## extra_newline_in_log_message.dump

    <SETUP CODE>
    echo "test file" >> test.txt
    svn add test.txt
    svn commit -m 'Adding test file.
    '
    <EXPORT CODE>

## property_change_on_root.dump

    <SETUP CODE>
    svn ps someproperty value .
    svn commit -m "Setting property on root."
    <EXPORT CODE>
