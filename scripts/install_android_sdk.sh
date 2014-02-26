#!/bin/sh

sudo apt-get update -q
if [ `uname -m` = x86_64 ]; then sudo apt-get install -qq libstdc++6:i386 lib32z1; fi

COMPONENTS=build-tools-19.0.2,extra-android-support,android-19,extra-android-m2repository,sysimg-19
curl -L https://raw.github.com/embarkmobile/android-sdk-installer/version-1/android-sdk-installer | bash /dev/stdin --install=$COMPONENTS
