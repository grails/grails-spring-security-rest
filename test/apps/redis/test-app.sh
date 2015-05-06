#!/bin/bash

./grailsw test-app --echoOut
if [ $? -ne 0 ]; then
    echo -e "\033[0;31mTests FAILED\033[0m"
    exit -1
fi