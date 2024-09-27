#!/bin/bash
set -e
set -x

if [[ $TRAVIS_PULL_REQUEST == 'false' ]]; then

    ./gradlew asciidoctor aggregateGroovyDoc

    version=`cat build.gradle | grep "version \"" | sed -n 's/^[ \t]*version\ "//pg' | sed -n 's/"//pg'`
    echo "Preparing release of version $version"

    echo "Configuring git with name ${GIT_NAME} and email ${GIT_EMAIL}"
    git config --global user.name "$GIT_NAME"
    git config --global user.email "$GIT_EMAIL"
    git config --global credential.helper "store --file=~/.git-credentials"
    echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

    git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git gh-pages > /dev/null

    cd gh-pages

    ./gradlew generateIndex

    # If there is a tag present then this becomes the latest
    if [[ -n $TRAVIS_TAG ]]; then
        rm -rf latest/
        mkdir -p latest/docs/gapi
        cp -r ../spring-security-rest-docs/build/docs/. ./latest/docs
        cp -r ../build/docs/groovydoc/. latest/docs/gapi
        git add latest/*

        rm -rf "$version"
        mkdir -p "$version/docs/gapi"
        cp -r ../spring-security-rest-docs/build/docs/. "$version/docs"
        cp -r ../build/docs/groovydoc/. "$version/docs/gapi"
        git add "$version/*"
    fi

    if [[ $TRAVIS_BRANCH == 'develop' && $TRAVIS_REPO_SLUG == 'alvarosanchez/grails-spring-security-rest' ]]; then
        rm -rf next/
        mkdir -p next/docs/gapi
        cp -r ../spring-security-rest-docs/build/docs/. ./next/docs
        cp -r ../build/docs/groovydoc/. next/docs/gapi
        git add next/*
    fi

    git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
    git push origin HEAD
    cd ..
    rm -rf gh-pages

fi
