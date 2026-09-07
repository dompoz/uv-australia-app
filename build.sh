#!/usr/bin/env bash
set -euo pipefail

GRADLE="/home/dom/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle"
export JAVA_HOME=/usr/lib/jvm/java-17-temurin
export ANDROID_HOME=/home/dom/Android/Sdk

DEBUG_APK="app/build/outputs/apk/debug/app-debug.apk"
MINIFIED_APK="app/build/outputs/apk/debugMinified/app-debugMinified.apk"
RELEASE_AAB="app/build/outputs/bundle/release/app-release.aab"

cd "$(dirname "$0")"

case "${1:-build}" in
  build)
    echo "Building debug APK and release AAB..."
    "$GRADLE" --no-build-cache :app:assembleDebug :app:bundleRelease

    echo ""
    echo "Outputs:"
    echo "  APK: $DEBUG_APK"
    echo "  AAB: $RELEASE_AAB"
    ;;

  deploy)
    DEVICE="${2:-}"

    echo "Building debugMinified APK..."
    "$GRADLE" --no-build-cache :app:assembleDebugMinified

    echo "Uninstalling existing app from device (if present)..."
    adb ${DEVICE:+-s "$DEVICE"} uninstall com.pinktakhyper.uvaustralia 2>/dev/null || true

    echo "Installing debugMinified APK..."
    adb ${DEVICE:+-s "$DEVICE"} install "$MINIFIED_APK"

    echo ""
    echo "debugMinified build installed successfully."
    ;;

  *)
    echo "Usage: $0 [build|deploy [adb-device-id]]"
    exit 1
    ;;
esac
