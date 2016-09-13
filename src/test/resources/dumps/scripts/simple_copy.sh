#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh


cd svn-test/checkout/testrepo

mkdir -p trunk/innerdir branches/mybranch/innerdir
echo "this is a test file" > trunk/innerdir/README.txt
svn add $SVNFLAGS trunk branches
svn commit $SVNFLAGS -m "Initial commit."
svn copy $SVNFLAGS trunk/innerdir/README.txt branches/mybranch/innerdir/README.txt
svn commit $SVNFLAGS -m "Copying file."
cd ..

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
