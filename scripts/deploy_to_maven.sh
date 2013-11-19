#!/bin/sh

BRANCH=`git rev-parse --abbrev-ref HEAD`
if [ "${TRAVIS_PULL_REQUEST}" = "false" ] && [ "${BRANCH}" = "master" ]; then
  TERM=dumb ./gradlew -PnexusUsername=${SONATYPE_USER} -PnexusPassword=${SONATYPE_PASS} upload
fi
