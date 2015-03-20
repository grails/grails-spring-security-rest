#!/bin/bash
set -e

git config --global user.name "$GIT_NAME"
git config --global user.email "$GIT_EMAIL"
git config --global credential.helper "store --file=~/.git-credentials"
echo "https://$GH_TOKEN:@github.com" > ~/.git-credentials

./grailsw doc

version=`cat SpringSecurityRestGrailsPlugin.groovy | grep version | sed -e 's/^.*"\(.*\)"$/\1/g'`
find target/docs/guide -name "*.html" | xargs sed -e "s/&#123;&#123;VERSION&#125;&#125;/${version}/g" -i
cp index.tmpl index.html
sed -e "s/{{VERSION}}/${version}/g" -i index.html


if [[ $TRAVIS_PULL_REQUEST == 'false' ]]; then

	# If there is a tag present then this becomes the latest
	if [[ -n $TRAVIS_TAG ]]; then
		git clone https://${GH_TOKEN}@github.com/${TRAVIS_REPO_SLUG}.git -b gh-pages gh-pages --single-branch > /dev/null
		cd gh-pages

		milestone=${version:5}
		if [[ -n $milestone ]]; then
			git rm -rf latest/
			mkdir -p latest
			cp -r ../build/docs/. ./latest/
			git add latest/*
		fi

		majorVersion=${version:0:4}
		majorVersion="${majorVersion}x"

		mkdir -p "$version"
		cp -r ../build/docs/. "./$version/"
		git add "$version/*"

		mkdir -p "$majorVersion"
		cp -r ../build/docs/. "./$majorVersion/"
		git add "$majorVersion/*"
		git commit -a -m "Updating docs for Travis build: https://travis-ci.org/$TRAVIS_REPO_SLUG/builds/$TRAVIS_BUILD_ID"
		git push origin HEAD
		cd ..
		rm -rf gh-pages

	fi

fi

echo "Replacing version in index.html...\n"

echo "Moving the generated documentation to the right place...\n"
rm -rf docs/
mv /tmp/docs .

#add, commit and push files
echo "Commiting and pushing...\n"
git add -f .
git commit -m "Documentation updated"
git push -fq origin gh-pages > /dev/null

echo "Done updating documentation\n"
