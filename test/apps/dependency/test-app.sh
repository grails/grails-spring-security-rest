#!/bin/bash

rm -rf foo && cd ../../../ && ./grailsw create-app foo && mv foo test/apps/dependency && cd test/apps/dependency && cp BuildConfig.groovy foo/grails-app/conf && cd foo && ./grailsw test-app
