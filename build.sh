#!/usr/bin/env bash
set -euo pipefail

GRADLE="/home/dom/.gradle/wrapper/dists/gradle-8.13-bin/5xuhj0ry160q40clulazy9h7d/gradle-8.13/bin/gradle"
export JAVA_HOME=/usr/lib/jvm/java-17-temurin
export ANDROID_HOME=/home/dom/Android/Sdk

cd "$(dirname "$0")"

echo "Building debug APK and release AAB..."
"$GRADLE" --no-build-cache :app:assembleDebug :app:bundleRelease

echo ""
echo "Outputs:"
echo "  APK: app/build/outputs/apk/debug/app-debug.apk"
echo "  AAB: app/build/outputs/bundle/release/app-release.aab"
