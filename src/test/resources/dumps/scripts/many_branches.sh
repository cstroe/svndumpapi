#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

mkdir trunk branches
echo "this is a test file" > file.txt
svn add $SVNFLAGS trunk branches
svn commit $SVNFLAGS -m "Initial commit."

svn cp $SVNFLAGS trunk branches/branch1
svn commit $SVNFLAGS -m "Branch 1."



cd $SCRIPT_DIR
./export.sh
cd $CURDIR
