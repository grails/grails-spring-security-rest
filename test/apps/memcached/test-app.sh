#!/bin/bash

rm -f grails-app/conf/Config.groovy \
    && for config in `ls grails-app/conf/Config*.groovy`; do
        cp $config grails-app/conf/Config.groovy \
           && ./grailsw clean-all \
           && ./grailsw compile \
           && ./grailsw test-app --echoOut \
           && rm grails-app/conf/Config.groovy
           if [ $? -ne 0 ]; then
                echo -e "\033[0;31mTests FAILED\033[0m"
                exit -1
           fi
       done
