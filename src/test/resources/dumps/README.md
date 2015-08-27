# How to create the test SVN Dump Files

## empty.dump

    mkdir -p svn-test/repos/testrepo
    cd svn-test/repos
    svnadmin create testrepo
    svnadmin dump testrepo > empty.dump
    
## firstcommit.dump

    mkdir -p svn-test/repos/testrepo
    mkdir -p svn-test/checkout
    cd svn-test/repos
    svnadmin create testrepo
    cd ../checkout
    bash -c 'svn checkout "file:///$(dirname $(pwd))/repos/testrepo" testrepo'
    cd testrepo
    touch firstFile.txt
    svn add firstFile.txt
    svn commit -m "Added a first file."
    cd ../../repos
    svnadmin dump testrepo > firstcommit.dump
    
## add_file.dump

    mkdir -p svn-test/repos/testrepo
    mkdir -p svn-test/checkout
    cd svn-test/repos
    svnadmin create testrepo
    cd ../checkout
    bash -c 'svn checkout "file:///$(dirname $(pwd))/repos/testrepo" testrepo'
    cd testrepo
    echo "this is a test file" > README.txt
    svn add README.txt
    svn commit -m "Committed README.txt"
    svnadmin dump testrepo > add_file.dump

## different_node_order.dump

This file was created from a bigger dump of the AgreementMaker SVN repo.
The main difference here is that the order of the "Node-kind" and "Node-path"
are different than `add_file.dump` ("Node-kind" is before "Node-path").