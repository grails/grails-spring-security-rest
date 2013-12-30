#!/bin/sh

grails doc
mv target/docs /tmp
git checkout gh-pages
git mv /tmp/docs .
git add .
git commit -m "Documentation updated" .
git push origin gh-pages
git checkout master