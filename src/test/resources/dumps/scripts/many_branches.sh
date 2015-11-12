#!/bin/bash -e

SVNFLAGS="-q"

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR
./setup.sh

cd svn-test/checkout/testrepo

mkdir trunk branches
echo "this is a test file" > trunk/file.txt
svn add $SVNFLAGS trunk branches
svn commit $SVNFLAGS -m "Initial commit."

# create branch1
svn cp $SVNFLAGS trunk branches/branch1
svn commit $SVNFLAGS -m "Branch 1."

# commit to trunk
cd ..
svn checkout $SVNFLAGS "file:///$SCRIPT_DIR/svn-test/repository/testrepo/trunk" trunk
cd trunk
echo "another line" >> file.txt
svn commit $SVNFLAGS -m "Adding another line to file.txt"
cd ..

# merge trunk into branch
svn checkout $SVNFLAGS "file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1" branch1
cd branch1
svn merge $SVNFLAGS "file:///$SCRIPT_DIR/svn-test/repository/testrepo/trunk" .
svn commit $SVNFLAGS -m "Merged trunk into branch1."
cd ..

# create branch2
cd testrepo
svn up $SVNFLAGS
svn cp $SVNFLAGS trunk branches/branch2
svn commit $SVNFLAGS -m "Branch 2."
cd ..

# make another change in branch1
cd branch1
svn up $SVNFLAGS
echo "third line" >> file.txt
svn commit $SVNFLAGS -m "Added third line."
cd ..

# merge branch1 into branch2
svn checkout $SVNFLAGS "file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch2" branch2
cd branch2
svn up $SVNFLAGS
svn merge $SVNFLAGS "file://$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1" .
svn commit $SVNFLAGS -m "Merged branch1 into branch2."
cd ..

# another change into branch1
cd branch1
echo "fourth line" >> file.txt
svn commit $SVNFLAGS -m "Added fourth line."
cd ..

# merge branch1 into branch2
cd branch2
svn up $SVNFLAGS
svn merge $SVNFLAGS "file://$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1" .
svn commit $SVNFLAGS -m "Merged branch1 into branch2."
cd ..

# merge branch1 back into trunk
cd branch1
svn up $SVNFLAGS
svn merge $SVNFLAGS "file:///$SCRIPT_DIR/svn-test/repository/testrepo/trunk" .
svn commit $SVNFLAGS -m "Last change on branch1."
svn switch $SVNFLAGS "file:///$SCRIPT_DIR/svn-test/repository/testrepo/trunk" .
svn merge $SVNFLAGS --reintegrate "file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1"
svn commit $SVNFLAGS -m "Merge branch1 back into trunk."
cd ..

# delete branch1
svn rm $SVNFLAGS -m "Deleting branch1." "file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch1"

# more work on trunk
cd trunk
svn up $SVNFLAGS
echo "a new file" > other.txt
svn add $SVNFLAGS other.txt
svn commit $SVNFLAGS -m "Adding another file."
cd ..

# merge trunk into branch2
cd branch2
svn up $SVNFLAGS
svn merge $SVNFLAGS "file://$SCRIPT_DIR/svn-test/repository/testrepo/trunk" .
svn commit $SVNFLAGS -m "Merged trunk into branch2."

# some work on branch2
echo "final touches" >> file.txt
svn commit $SVNFLAGS -m "Final touches on branch2."

# merge trunk into branch2 one last time
svn up $SVNFLAGS
svn merge $SVNFLAGS "file://$SCRIPT_DIR/svn-test/repository/testrepo/trunk" .
svn commit $SVNFLAGS -m "Merged trunk into branch2 final."
cd ..

# merge branch2 back into trunk
cd trunk
svn up $SVNFLAGS
svn merge $SVNFLAGS --reintegrate "file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch2"
svn commit $SVNFLAGS -m "Merged branch2 back into trunk."
cd ..

# delete branch2
svn rm $SVNFLAGS -m "Deleting branch2." "file:///$SCRIPT_DIR/svn-test/repository/testrepo/branches/branch2"

# work continues on trunk
cd trunk
svn up $SVNFLAGS
svn rm $SVNFLAGS other.txt
echo "P.S. really last line" >> file.txt
svn commit $SVNFLAGS -m "Some work."
cd ..

# uncomment to stop execution here
#/bin/false

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
