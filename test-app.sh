#!/bin/bash

set -e

[[ ! -z "$BINTRAY_KEY" ]] && echo "bintrayKey=$BINTRAY_KEY" >> ~/.gradle/gradle.properties
[[ ! -z "$PLUGIN_PORTAL_PASSWORD" ]] && echo "pluginPortalPassword=$PLUGIN_PORTAL_PASSWORD" >> ~/.gradle/gradle.properties

rm -rf build/
mkdir build
./gradlew clean build exportVersion
export pluginVersion=`cat spring-security-rest/build/version.txt`
export grailsVersion=`cat spring-security-rest-testapp-profile/gradle.properties | grep grailsVersion | sed -n 's/^grailsVersion=//p'`

echo "Plugin version: $pluginVersion. Grails version for test apps: $grailsVersion"
source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk use grails $grailsVersion

./gradlew check install \
  && cd build && rm -rf * \
  && for feature in `ls ../spring-security-rest-testapp-profile/features/`; do
     grails create-app -profile org.grails.plugins:spring-security-rest-testapp-profile:$pluginVersion -features $feature $feature && cd $feature && ./gradlew check && cd ..
     if [ $? -ne 0 ]; then
       echo -e "\033[0;31mTests FAILED\033[0m"
       cd ..
       ./upload-test-reports.sh
       exit -1
     fi
     done \
  && cd .. \
  && ./gradlew artifactoryPublish