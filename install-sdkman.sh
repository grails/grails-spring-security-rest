#!/usr/bin/env bash
set -e

rm -rf ~/.gvm
rm -rf ~/.sdkman
curl -s get.sdkman.io | bash
echo sdkman_auto_answer=true > ~/.sdkman/etc/config