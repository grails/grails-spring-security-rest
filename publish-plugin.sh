#!/usr/bin/env bash

[[ ! -z "$BINTRAY_KEY" ]] && echo "bintrayKey=$BINTRAY_KEY" >> ~/.gradle/gradle.properties
[[ ! -z "$PLUGIN_PORTAL_PASSWORD" ]] && echo "pluginPortalPassword=$BINTRAY_KEY" >> ~/.gradle/gradle.properties

echo "Publishing plugin"
./gradlew clean publishPlugin notifyPluginPortal
