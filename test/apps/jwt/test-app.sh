#!/bin/bash

rm -f grails-app/conf/application.groovy \
    && for config in `ls grails-app/conf/application*.groovy`; do
        cp $config grails-app/conf/application.groovy \
           && ./gradlew clean check \
           && rm grails-app/conf/application.groovy
           if [ $? -ne 0 ]; then
                echo -e "\033[0;31mTests FAILED\033[0m"
                exit -1
           fi
       done
