#!/usr/bin/expect -f

# Script installs Android SDK components

spawn android update sdk --filter tools,platform-tools,build-tools-19.0.1,extra-android-support,android-19,extra-android-m2repository --no-ui --force --all
expect "Do you accept the license *:"
send -- "y\r"
interact
