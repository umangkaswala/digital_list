---
name: android-build-verify
description: Build, install, launch, and screenshot-verify the Digital List Android app (com.stackpointer.list) on the local emulator. Use this whenever the user asks to run, build, install, launch, test on device/emulator, or visually verify the Digital List app — including after any UI/theme/screen change, at the end of a build milestone, or when checking whether something "actually works" rather than just compiles. Also use it proactively after writing or editing Compose screens for this app, before reporting a UI change as done. Encodes this machine's Gradle/JDK/SDK environment quirks (loopback-connection daemon failures, adb/MSYS path mangling) so they don't need to be rediscovered.
---

# Android build, run, and verify (Digital List)

This project's build plan (`design-handoff/BUILD_PLAN.md`) ends every milestone with "something
runnable on a device" — this skill is that verification step. Don't report a UI or behavior
change as done without actually running it through this skill first; compiling is not the same
as working, and several early bugs in this project (a mis-timed screenshot, a stale build) were
only caught by actually looking at the rendered screen.

## Quick path: the bundled script

`scripts/build-and-verify.sh` runs the whole sequence — boot a device if none is attached,
build, install, launch, wait out the slow first frame, screenshot, pull, and sanity-check that
the process didn't crash. Run it from Git Bash:

```
bash .claude/skills/android-build-verify/scripts/build-and-verify.sh [avd-name] [activity]
```

Defaults: AVD `Medium_Phone_API_36.1`, activity `.MainActivity`. It leaves the screenshot at
`C:/gtmp/verify_screen.png` — **read that file** (the Read tool renders PNGs) to actually check
the result against `design-handoff/screenshots/` or the relevant `SCREENS.md` entry. The script
finishing without error means "it launched and didn't crash," not "it looks right" — the visual
check is still a separate, necessary step.

If the script fails, or you need a single step in isolation (e.g. just a fresh screenshot
without rebuilding), use the manual steps below — they're the same sequence, unrolled.

## Why the environment needs these specific fixes

These aren't generic Android advice — they're specific failures hit and fixed while building
this project, and they will recur identically if skipped:

- **`java.io.IOException: Unable to establish loopback connection`** on *any* `gradlew`
  invocation, including bare `gradlew wrapper`. Root cause: the JDK's daemon IPC pipe fails to
  bind when `TEMP`/`TMP` is a long or space-containing path (this machine's default user
  profile has both). Fix: point `TEMP`, `TMP`, and `GRADLE_OPTS=-Djava.io.tmpdir=...` at a
  short path (`C:\gtmp`) before invoking Gradle. This is not optional — every `gradlew` call
  needs it, not just the first one per session, since env vars don't persist across tool calls.
- **`JAVA_HOME` is unset** — nothing resolves `java` on PATH. Android Studio's bundled JBR
  works fine: `C:\Program Files\Android\Android Studio\jbr`.
- **`adb`/`emulator` mangled paths from Git Bash** — MSYS auto-converts anything that looks
  like a Unix path into a Windows path before hand-off to native `.exe`s. This silently
  corrupts device-side paths like `/sdcard/screen.png` passed to `adb shell`/`adb pull`. Fix:
  `export MSYS2_ARG_CONV_EXCL="/sdcard/"` before those calls, so only the device-side argument
  is exempted — the local destination path still needs normal conversion.
- **A cold install's first frame is slow** (several seconds of JIT/inflate — shows as a
  `Davey!` frame in logcat). A screenshot taken too early catches the splash icon, not real
  content, and looks like a bug that isn't one. Wait ~5s after `am start` before capturing, and
  if a screenshot looks wrong, check logcat for an actual crash before assuming one — don't
  conclude "broken" from a single early screenshot.

## Manual steps (what the script automates)

Environment, once per shell:
```
export JAVA_HOME="C:\Program Files\Android\Android Studio\jbr"
export TMP="C:/gtmp"; export TEMP="C:/gtmp"; export GRADLE_OPTS="-Djava.io.tmpdir=C:/gtmp"
export ANDROID_HOME="$LOCALAPPDATA/Android/Sdk"
```

Check/boot a device (AVDs on this machine: `Pixel_9`, `Pixel_9_Pro`, `Medium_Phone_API_36.1`):
```
"$ANDROID_HOME/platform-tools/adb.exe" devices
# if empty:
nohup "$ANDROID_HOME/emulator/emulator.exe" -avd Medium_Phone_API_36.1 -no-snapshot -netdelay none -netspeed full &
"$ANDROID_HOME/platform-tools/adb.exe" wait-for-device
until [ "$("$ANDROID_HOME/platform-tools/adb.exe" shell getprop sys.boot_completed | tr -d '\r')" = "1" ]; do sleep 3; done
```

Build, install, launch (run from the repo root):
```
./gradlew.bat assembleDebug --no-daemon
"$ANDROID_HOME/platform-tools/adb.exe" install -r app/build/outputs/apk/debug/app-debug.apk
"$ANDROID_HOME/platform-tools/adb.exe" shell am force-stop com.stackpointer.list
"$ANDROID_HOME/platform-tools/adb.exe" shell am start -n com.stackpointer.list/.MainActivity
sleep 5
```

Screenshot + pull:
```
export MSYS2_ARG_CONV_EXCL="/sdcard/"
"$ANDROID_HOME/platform-tools/adb.exe" shell screencap -p /sdcard/screen.png
"$ANDROID_HOME/platform-tools/adb.exe" pull /sdcard/screen.png /c/gtmp/screen.png
```
Then use the Read tool on `C:\gtmp\screen.png` to actually look at it.

If something looks wrong, check for a crash before assuming the UI is broken:
```
"$ANDROID_HOME/platform-tools/adb.exe" logcat -d --pid=$("$ANDROID_HOME/platform-tools/adb.exe" shell pidof com.stackpointer.list | tr -d '\r')
```

## Current project constants

These are this repo's values right now — update this section (and the script's defaults) if
they change:

- Package / application id: `com.stackpointer.list`
- Main activity: `com.stackpointer.list.MainActivity`
- `compileSdk` / `targetSdk`: 37, `minSdk`: 31
- Gradle wrapper: 9.7.0 (AGP 9.3.1, which requires Gradle ≥ 9.5 and has Kotlin support built in
  — no separate `org.jetbrains.kotlin.android` plugin)
