#!/bin/bash

./grailsw test-app \
  && cd test/apps \
  && for app in `ls .`; do
     cd $app && ./test-app.sh
     if [ $? -ne 0 ]; then
        echo -e "\033[0;31mTests FAILED\033[0m"
        exit -1
     fi
     done \
  && cd ../../../
