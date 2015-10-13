#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh

cd svn-test/checkout/testrepo
mkdir trunk
mkdir branches
cd trunk
mkdir dir1
cd dir1
echo "this is a test file" > file1.txt
cd ../..
svn add $SVNFLAGS branches trunk
svn commit -m "Initial commit." $SVNFLAGS

svn cp file://$SCRIPT_DIR/svn-test/repository/testrepo/trunk \
  file://$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1 -m "Create branch." \
  $SVNFLAGS

cd $SCRIPT_DIR
cd svn-test/checkout/testrepo
svn up $SVNFLAGS
cd trunk/dir1
svn rm $SVNFLAGS file1.txt
svn cp $SVNFLAGS file://$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1/dir1/file1.txt@2 .
svn commit -m "Copy from branch." $SVNFLAGS

echo "changed file" > file1.txt
svn commit -m "Changed file." $SVNFLAGS

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
