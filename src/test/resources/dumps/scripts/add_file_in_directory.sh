#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

mkdir -p dir1/dir2/dir3
svn add $SVNFLAGS dir1
svn commit $SVNFLAGS -m "Adding directories."

echo $1 > dir1/dir2/dir3/README.txt
svn add $SVNFLAGS dir1/dir2/dir3/README.txt
svn commit $SVNFLAGS -m "Committed README.txt"

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
