#!/bin/bash
# @author: kong <congcoi123@gmail.com>

# A utility bash for checking the name of the current OS

if [[ "$OSTYPE" == "linux-gnu" ]]; then
        # Linux
        echo "linux"
elif [[ "$OSTYPE" == "darwin"* ]]; then
        # Mac OSX
        echo "macos"
elif [[ "$OSTYPE" == "cygwin" ]]; then
        # POSIX compatibility layer and Linux environment emulation for Windows
        echo "posix"
elif [[ "$OSTYPE" == "msys" ]]; then
        # Lightweight shell and GNU utilities compiled for Windows (part of MinGW)
        echo "mingw"
elif [[ "$OSTYPE" == "win32" ]]; then
        # Window
        echo "window"
elif [[ "$OSTYPE" == "freebsd"* ]]; then
        # Freebsd
        echo "freebsd"
else
        echo "unknown"
fi
