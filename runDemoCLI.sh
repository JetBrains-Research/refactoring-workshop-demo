#!/usr/bin/env bash

# https://stackoverflow.com/a/246128
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
if uname -s | grep -iq cygwin ; then
    DIR=$(cygpath -w "$DIR")
    PWD=$(cygpath -w "$PWD")
fi

"$DIR/gradlew" -p "$DIR" runDemoPluginCLI -Pinput="$1" -Poutput="$2"