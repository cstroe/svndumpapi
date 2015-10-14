#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh


cd svn-test/checkout/testrepo

mkdir -p trunk/innerdir branches
echo "this is a test file" > trunk/innerdir/README.txt
svn add $SVNFLAGS trunk branches
svn commit $SVNFLAGS -m "Initial commit."
svn copy $SVNFLAGS trunk branches/mybranch
svn commit $SVNFLAGS -m "Creating branch."
cd ..

svn checkout $SVNFLAGS \
  file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/mybranch mybranch

cd mybranch
echo "branch work" >> innerdir/README.txt
svn commit $SVNFLAGS -m "Branch work."
cd ..

svn checkout $SVNFLAGS file:///$SCRIPT_DIR/svn-test/repository/testrepo/trunk trunk
cd trunk
svn merge $SVNFLAGS --reintegrate ^/branches/mybranch
svn commit $SVNFLAGS -m "Merge branch back into trunk."
svn delete $SVNFLAGS ^/branches/mybranch -m "Removing branch."

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
