#!/bin/bash

rm -rf $HOME/.grails/ivy-cache/org.grails.plugins
rm -rf $HOME/.m2/repository/org/grails/plugins

./grailsw test-app -coverage -xml \
  && cd test/apps \
  && for app in `ls .`; do
     cd $app && ./test-app.sh && cd ..
     if [ $? -ne 0 ]; then
        echo -e "\033[0;31mTests FAILED\033[0m"
        exit -1
     fi
     done \
  && cd ../../ \
  && echo `pwd` \
  && ./grailsw cobertura-merge \
  && ./grailsw coveralls
