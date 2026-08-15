#!/usr/bin/env bash
# Build the debug APK, install it on an emulator, launch it, and pull a screenshot.
# Usage: build-and-verify.sh [avd-name] [activity]
#   avd-name  defaults to Medium_Phone_API_36.1
#   activity  defaults to .MainActivity (relative to $PACKAGE_ID below)
set -euo pipefail

AVD_NAME="${1:-Medium_Phone_API_36.1}"
ACTIVITY="${2:-.MainActivity}"
PACKAGE_ID="com.stackpointer.list"

REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../../../.." && pwd)"
cd "$REPO_ROOT"

# --- Windows/JDK environment fixes (see SKILL.md for why each of these exists) ---
export JAVA_HOME="${JAVA_HOME:-C:\Program Files\Android\Android Studio\jbr}"
SHORT_TMP="C:/gtmp"
mkdir -p "$SHORT_TMP" 2>/dev/null || mkdir -p /c/gtmp
export TMP="$SHORT_TMP"
export TEMP="$SHORT_TMP"
export GRADLE_OPTS="-Djava.io.tmpdir=$SHORT_TMP"

ANDROID_HOME="${ANDROID_HOME:-$LOCALAPPDATA/Android/Sdk}"
ADB="$ANDROID_HOME/platform-tools/adb.exe"
EMULATOR="$ANDROID_HOME/emulator/emulator.exe"

echo "== Repo root: $REPO_ROOT"
echo "== ANDROID_HOME: $ANDROID_HOME"

# --- 1. Make sure a device is up ---
if ! "$ADB" devices | grep -q "device$"; then
  echo "== No device attached, booting $AVD_NAME in the background..."
  nohup "$EMULATOR" -avd "$AVD_NAME" -no-snapshot -netdelay none -netspeed full \
    > /c/gtmp/emulator.log 2>&1 &
  "$ADB" wait-for-device
  echo "== Device connected, waiting for boot to finish..."
  until [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ]; do
    sleep 3
  done
fi
echo "== Device ready:"
"$ADB" devices

# --- 2. Build ---
echo "== Building debug APK..."
./gradlew.bat assembleDebug --no-daemon

APK="app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK" ]; then
  echo "ERROR: expected APK not found at $APK" >&2
  exit 1
fi

# --- 3. Install + launch ---
echo "== Installing and launching..."
"$ADB" install -r "$APK"
"$ADB" shell am force-stop "$PACKAGE_ID"
"$ADB" shell am start -n "$PACKAGE_ID/$ACTIVITY"

# First frame after a cold install can take several seconds (JIT/inflate) — give it
# room before screenshotting, or the capture catches the splash icon, not real content.
sleep 5

# --- 4. Screenshot + pull ---
# /sdcard/... must be excluded from MSYS's automatic unix->Windows path conversion,
# or adb.exe receives a mangled local path instead of the on-device path.
export MSYS2_ARG_CONV_EXCL="/sdcard/"
OUT_DIR="/c/gtmp"
mkdir -p "$OUT_DIR"
"$ADB" shell screencap -p /sdcard/verify_screen.png
"$ADB" pull /sdcard/verify_screen.png "$OUT_DIR/verify_screen.png"

echo "== Screenshot saved to $OUT_DIR/verify_screen.png"

# --- 5. Crash / slow-first-frame sanity check ---
PID="$("$ADB" shell pidof "$PACKAGE_ID" 2>/dev/null | tr -d '\r')"
if [ -z "$PID" ]; then
  echo "WARNING: $PACKAGE_ID is not running — it likely crashed. Recent logcat:" >&2
  "$ADB" logcat -d -t 200 | grep -iE "FATAL|AndroidRuntime|$PACKAGE_ID" >&2 || true
  exit 1
fi

echo "== Done. Read $OUT_DIR/verify_screen.png to check the result."
