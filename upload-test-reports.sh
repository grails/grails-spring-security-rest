#!/usr/bin/env bash

echo "Configuring git with name ${GIT_NAME} and email ${GIT_EMAIL}"
git config --global user.name "$GIT_NAME"
git config --global user.email "$GIT_EMAIL"
git config --global credential.helper "store --file=~/.git-credentials"
echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git gh-pages > /dev/null

cd gh-pages
rm -rf test-reports
for feature in `ls ../spring-security-rest-testapp-profile/features/`; do
    mkdir -p test-reports/$feature/reports
    mkdir test-reports/$feature/geb-reports
    cp -R ../build/$feature/build/reports/tests/* test-reports/$feature/reports
    cp -R ../build/$feature/build/geb-reports/* test-reports/$feature/geb-reports
done

git commit -a -m "Uploading test reports for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
git push origin HEAD
cd ..
rm -rf gh-pages