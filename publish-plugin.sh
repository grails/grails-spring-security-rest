#!/usr/bin/env bash

echo "Publishing plugin"
rm -rf build/
./gradlew clean publishPlugin --stacktrace
