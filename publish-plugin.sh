#!/usr/bin/env bash

echo "Publishing plugin"
./gradlew clean publishPlugin notifyPluginPortal -x :spring-security-rest-testapp-profile:notifyPluginPortal --stacktrace
