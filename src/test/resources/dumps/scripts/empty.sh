#!/bin/bash
CURDIR=`pwd`
cd $(dirname $0)
./setup.sh
./export.sh
cd $CURDIR

