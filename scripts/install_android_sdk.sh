#!/bin/sh

sudo apt-get update -q

cd $HOME
if [ `uname -m` = x86_64 ]; then sudo apt-get install -qq libstdc++6:i386 lib32z1; fi
wget -O android-sdk.tgz http://dl.google.com/android/android-sdk_r22.3-linux.tgz
tar xzf android-sdk.tgz
cd -

sudo apt-get install expect

INSTALL_COMPONENTS_SCRIPT="./scripts/install_android_sdk_components.sh"
chmod +x ${INSTALL_COMPONENTS_SCRIPT}
${INSTALL_COMPONENTS_SCRIPT}
