#!/usr/bin/env bash

DIFF_DIR="$(cd $1 && pwd)"
cd "$DIFF_DIR" && find . -type f | grep -vE "^\./\.git" | sort | xargs -n1 -exec md5sum
