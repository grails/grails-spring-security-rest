#!/usr/bin/env bash


echo "Creating folder target/divshot/$TRAVIS_JOB_NUMBER"
mkdir -p target/divshot/$TRAVIS_JOB_NUMBER
mv target/test-reports target/divshot/$TRAVIS_JOB_NUMBER/core
for app in `ls test/apps` ; do mv test/apps/$app/target/test-reports target/divshot/$TRAVIS_JOB_NUMBER/$app ; done
cd target/divshot

echo "Renaming files with spaces"
../../renameFiles.groovy .

divshot -t $DIVSHOT_TOKEN config:add name spring-security-rest
divshot -t $DIVSHOT_TOKEN pull production
divshot -t $DIVSHOT_TOKEN push production
divshot -t $DIVSHOT_TOKEN files production
