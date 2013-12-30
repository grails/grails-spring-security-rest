#!/bin/sh

grails doc
rm -rf /tmp/docs/
mv target/docs /tmp
git checkout gh-pages
rm -rf docs/
git mv /tmp/docs .
git add -A
git commit -m "Documentation updated"
git push origin gh-pages
git checkout master