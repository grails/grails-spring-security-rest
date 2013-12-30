#!/bin/sh

git checkout gh-pages
git merge master
grails doc
cp -R target/docs/* .
git add .
git commit -m "Documentation updated" .
git push origin gh-docs
git checkout master