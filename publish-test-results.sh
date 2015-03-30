#!/usr/bin/env bash

mkdir target/divshot
mv target/test-reports target/divshot/core
for app in `ls test/apps` ; do mv test/apps/$app/target/test-reports target/divshot/$app ; done
cd target/divshot
divshot -t $DIVSHOT_TOKEN config:add name spring-security-rest
divshot -t $DIVSHOT_TOKEN push production
