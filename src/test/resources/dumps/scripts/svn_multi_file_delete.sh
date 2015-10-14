#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh

cd svn-test/checkout/testrepo

echo "this is a test file" > README.txt
echo "this is a test file" > README2.txt
echo "this is a test file" > README3.txt
svn add $SVNFLAGS README.txt README2.txt README3.txt 
svn commit $SVNFLAGS -m "Added 3 files."
svn rm $SVNFLAGS README.txt README2.txt README3.txt 
svn commit $SVNFLAGS -m "Deleted 3 files."
                          
cd $SCRIPT_DIR
./export.sh
cd $CURDIR
