#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh

cd svn-test/checkout/testrepo
echo "this is a test file" > file1.txt
svn add $SVNFLAGS file1.txt
svn commit -m "This commit makes me happy ☺" $SVNFLAGS

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
