#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
SDK="$(xcrun --sdk iphoneos --show-sdk-path)"
mkdir -p build
xcrun --sdk iphoneos clang -arch arm64 -isysroot "$SDK" \
  -miphoneos-version-min=13.0 -fobjc-arc -fmodules \
  -Wall -Wextra -Wno-unused-parameter \
  -dynamiclib -install_name '@rpath/VTUANKeyGate.dylib' \
  -framework UIKit -framework Foundation -framework Security \
  -o build/VTUANKeyGate.dylib src/VTUANKeyGate.m
xcrun vtool -show-build build/VTUANKeyGate.dylib
xcrun otool -L build/VTUANKeyGate.dylib
file build/VTUANKeyGate.dylib
