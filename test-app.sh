#!/bin/sh

./grailsw test-app \
  && cd test/apps \
  && for app in `ls .`; do
     cd $app \
        && rm -f grails-app/conf/Config.groovy \
        && for config in `ls grails-app/conf/Config*.groovy`; do
           cp $config grails-app/conf/Config.groovy \
           && ../../../grailsw test-app --echoOut
           if [ $? -ne 0 ]; then
              exit $?
           fi
           done
     done \
  && cd ../../../
