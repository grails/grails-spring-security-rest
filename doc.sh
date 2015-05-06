#!/bin/bash
set -e

if [[ $TRAVIS_PULL_REQUEST == 'false' ]]; then

	# If there is a tag present then this becomes the latest
	if [[ -n $TRAVIS_TAG ]]; then
		./grailsw doc

		version=`cat SpringSecurityRestGrailsPlugin.groovy | grep version | sed -e 's/^.*"\(.*\)"$/\1/g'`
		find target/docs/guide -name "*.html" | xargs sed -e "s/&#123;&#123;VERSION&#125;&#125;/${version}/g" -i
		echo "Preparing release of version $version"

		echo "Configuring git with name ${GIT_NAME} and email ${GIT_EMAIL}"
		git config --global user.name "$GIT_NAME"
		git config --global user.email "$GIT_EMAIL"
		git config --global credential.helper "store --file=~/.git-credentials"
		echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

		git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git gh-pages > /dev/null

		cd gh-pages

		./gradlew generateIndex

		rm -rf latest/
		mkdir -p latest/docs
		cp -r ../target/docs/. ./latest/docs
		git add latest/*

		rm -rf "$version"
		mkdir -p "$version"
		mv ../target/docs "./$version/"
		git add "$version/*"

		git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
		git push origin HEAD
		cd ..
		rm -rf gh-pages
	fi

fi
