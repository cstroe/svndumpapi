#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

FILE_CONTENT="This is a text file."
NEW_FILE_CONTENT="This is a test file."
AGAIN_FILE_CONTENT="This is a test file!"

echo $FILE_CONTENT > README.txt
svn add $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Added readme." # r1

svn rm $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Deleting file." # r2

svn cp $SVNFLAGS README.txt@1 README.txt
echo $NEW_FILE_CONTENT > README.txt
svn commit $SVNFLAGS -m "Copied readme from r1." # r3

svn rm $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Deleting file." # r4

svn cp $SVNFLAGS README.txt@3 README.txt
echo $AGAIN_FILE_CONTENT > README.txt
svn commit $SVNFLAGS -m "Copied readme from r3." # r3

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
