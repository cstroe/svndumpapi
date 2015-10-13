#!/bin/bash

CURDIR=$(pwd)
SCRIPT_DIR=$(cd $(dirname $0) && pwd)
cd $SCRIPT_DIR

./setup.sh

touch svn-test/checkout/testrepo/firstFile.txt
cd svn-test/checkout/testrepo
svn add firstFile.txt > /dev/null
svn commit -m "Added a first file." > /dev/null

cd $SCRIPT_DIR
./export.sh
cd $CURDIR
