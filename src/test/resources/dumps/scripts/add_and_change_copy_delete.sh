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

echo $NEW_FILE_CONTENT > README.txt
svn commit $SVNFLAGS -m "Changed." #r2

echo $AGAIN_FILE_CONTENT > OTHER.txt
svn add $SVNFLAGS OTHER.txt
svn commit $SVNFLAGS -m "Other file." #r3

svn cp $SVNFLAGS README.txt@2 README-new.txt
echo $NEW_FILE_CONTENT > README.txt
svn commit $SVNFLAGS -m "Copied readme from r2." # r4

svn rm $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Deleted." #r5

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
