#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

FILE_CONTENT=$1

echo $1 > README.txt
svn add $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Added readme." # r1

svn rm $SVNFLAGS README.txt
svn commit $SVNFLAGS -m "Deleting file." # r2

svn cp $SVNFLAGS README.txt@1 OTHER.txt
svn commit $SVNFLAGS -m "Copied readme from r1." # r3

svn mkdir $SVNFLAGS dir1 
svn cp $SVNFLAGS OTHER.txt dir1/OTHER.txt
svn commit $SVNFLAGS -m "Copied readme again." # r4

svn rm $SVNFLAGS dir1
svn commit $SVNFLAGS -m "Deleting directory." # r5

svn cp $SVNFLAGS dir1@4 otherdir1
svn commit $SVNFLAGS -m "Copied directory from r4." # r6

svn cp $SVNFLAGS otherdir1/OTHER.txt otherdir1/NEWNAME.txt
svn commit $SVNFLAGS -m "Renamed file again." # r7


cd $SCRIPT_DIR
./export.sh
cd $CURDIR
