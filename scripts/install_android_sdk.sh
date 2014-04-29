#!/bin/sh

sudo apt-get update -q
sudo apt-get install -qq libstdc++6:i386 lib32z1 expect

COMPONENTS=build-tools-19.0.3,extra-android-support,android-19,extra-android-m2repository,sysimg-19
curl -L https://raw.github.com/embarkmobile/android-sdk-installer/version-2/android-sdk-installer | bash /dev/stdin --install=$COMPONENTS
