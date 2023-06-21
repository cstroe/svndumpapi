#!/usr/bin/env bash

SCRIPT_DIR=$(cd $(dirname $0) && pwd)

"$SCRIPT_DIR/sha_dir.sh" "$1" > /tmpfs/A
"$SCRIPT_DIR/sha_dir.sh" "$2" > /tmpfs/B

diff /tmpfs/A /tmpfs/B
