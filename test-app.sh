./grailsw test-app \
  && cd test/apps \
  && for app in `ls .`; do cd $app && ../../../grailsw test-app ; done \
  && cd ../../../
