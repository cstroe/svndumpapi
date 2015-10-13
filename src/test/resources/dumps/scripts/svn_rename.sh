#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

echo "this is a test file" > README.txt
svn add $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Committed README.txt"
svn mv $SVNFLAGS README.txt README-new.txt
svn commit $SVNFLAGS -m "Renamed README.txt to README-new.txt"

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
