#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

echo "new content" > README.txt
svn add $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Added readme."
svn cp $SVNFLAGS README.txt OTHER.txt
svn commit $SVNFLAGS -m "Copied readme."

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
