#!/bin/bash

set -e

echo "bintrayKey=$BINTRAY_KEY" >> ~/.gradle/gradle.properties

./gradlew clean install check license \
  && cd test/apps \
  && for app in `ls .`; do
     cd $app && ./test-app.sh && cd ..
     if [ $? -ne 0 ]; then
        echo -e "\033[0;31mTests FAILED\033[0m"
        exit -1
     fi
     done \
  && cd ../../

./gradlew artifactoryPublish