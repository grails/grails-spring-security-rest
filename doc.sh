#!/bin/sh

echo -e "Starting to update gh-pages\n"

rm -rf docs/
./grailsw doc
version=`cat SpringSecurityRestGrailsPlugin.groovy | grep version | sed -e 's/^.*"\(.*\)"$/\1/g'`
find target/docs/guide -name "*.html" | xargs sed -e "s/&#123;&#123;VERSION&#125;&#125;/${version}/g" -i ""

rm -rf /tmp/docs/
mv target/docs /tmp

#go to home and setup git
cd $HOME
git config --global user.email "travis@travis-ci.org"
git config --global user.name "Travis"

#using token clone gh-pages branch
git clone --quiet --branch=gh-pages https://${GH_TOKEN}@github.com/alvarosanchez/grails-spring-security-rest.git gh-pages > /dev/null

#go into that directory and copy data we're interested in to that directory
cd gh-pages

cp index.tmpl index.html
sed -e "s/{{VERSION}}/${version}/g" -i "" index.html

rm -rf docs/
mv /tmp/docs .

#add, commit and push files
git add -f .
git commit -m "Documentation updated"
git push -fq origin gh-pages > /dev/null

echo -e "Done magic with coverage\n"