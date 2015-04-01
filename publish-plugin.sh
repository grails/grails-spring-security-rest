#!/usr/bin/env bash

echo "grails.project.repos.grailsCentral.username = 'alvaro.sanchez'" >> ~/.grails/settings.groovy
echo "grails.project.repos.grailsCentral.username = '${PLUGIN_PORTAL_PASSWORD}'" >> ~/.grails/settings.groovy

./grailsw clean
./grailsw package-plugin

echo "Publishing plugin"
./grailsw publish-plugin