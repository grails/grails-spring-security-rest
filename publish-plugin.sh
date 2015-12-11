#!/usr/bin/env bash

echo "Publishing plugin"
./gradlew clean publishPlugin notifyPluginPortal --stacktrace
