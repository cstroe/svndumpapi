#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh

cd svn-test/checkout/testrepo
echo "this is a test file" > file1.txt
svn add $SVNFLAGS file1.txt
svn commit -m "Initial commit." $SVNFLAGS

echo "changed file" > file1.txt
svn commit -m "Changed file." $SVNFLAGS

echo "changed file again" > file1.txt
svn commit -m "Changed file again." $SVNFLAGS

echo "changed file a third time" > file1.txt
svn commit -m "Changed file a third time." $SVNFLAGS

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
