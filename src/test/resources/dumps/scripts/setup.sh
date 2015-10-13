#!/bin/bash

CURDIR=`pwd`

rm -fR svn-test
mkdir -p svn-test/repository/testrepo
mkdir -p svn-test/checkout
svnadmin create svn-test/repository/testrepo

cd svn-test/checkout
svn checkout "file:///$CURDIR/svn-test/repository/testrepo" testrepo > /dev/null

cd $CURDIR
