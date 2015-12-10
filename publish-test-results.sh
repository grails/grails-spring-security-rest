#!/usr/bin/env bash


echo "Creating folder build/divshot/$TRAVIS_JOB_NUMBER"
mkdir -p build/divshot/$TRAVIS_JOB_NUMBER
mv build/reports/tests build/divshot/$TRAVIS_JOB_NUMBER/core
for app in `ls test/apps`
do
    mv test/apps/$app/build/reports/tests build/divshot/$TRAVIS_JOB_NUMBER/$app
    echo "Tests reports are available at http://spring-security-rest.divshot.io/$TRAVIS_JOB_NUMBER/$app"
done
cd build/divshot

echo "Renaming files with spaces"
../../renameFiles.groovy .

divshot -t $DIVSHOT_TOKEN config:add name spring-security-rest
divshot -t $DIVSHOT_TOKEN pull production

echo "Removing old builds. Basically everything but ${TRAVIS_JOB_NUMBER/.*/}*"
for dir in `ls -1 -d */ | grep -v "${TRAVIS_JOB_NUMBER/.*/}"` ; do rm -rf $dir ; done

echo "Pusing files to divshot"
divshot -t $DIVSHOT_TOKEN push production
echo "Main test reports are available at http://spring-security-rest.divshot.io/$TRAVIS_JOB_NUMBER/core"
