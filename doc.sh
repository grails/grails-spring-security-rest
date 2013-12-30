#!/bin/sh

grails doc
version=`cat SpringSecurityRestGrailsPlugin.groovy | grep version | sed -e 's/^.*"\(.*\)"$/\1/g'`
find target/docs/guide -name "*.html" | xargs sed -i -e "s/&#123;&#123;VERSION&#125;&#125;/${version}/g"

rm -rf /tmp/docs/
mv target/docs /tmp

git checkout gh-pages
rm -rf docs/
mv /tmp/docs .
git add -A
git commit -m "Documentation updated"
git push origin gh-pages

git checkout master