#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

echo "this is a test file" > README.txt
svn add $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Added readme."
svn cp $SVNFLAGS README.txt OTHER.txt
svn commit $SVNFLAGS -m "Copied readme."

svn mkdir $SVNFLAGS dir1 
svn cp $SVNFLAGS OTHER.txt dir1/OTHER.txt
svn commit $SVNFLAGS -m "Copied readme again."

svn mv $SVNFLAGS dir1 otherdir1
svn commit $SVNFLAGS -m "Moved directory."

svn cp $SVNFLAGS otherdir1/OTHER.txt otherdir1/NEWNAME.txt
svn commit $SVNFLAGS -m "Renamed file again."


cd $SCRIPT_DIR
./export.sh
cd $CURDIR
