# 📱 apps/android-sample

Sample native Android app using Kotlin, Jetpack Compose, Hilt, and shared modules from the monorepo.

---

## Overview

- Language: Kotlin (JDK 17)
- UI: Jetpack Compose
- DI: Hilt
- Build: Gradle 8.12 (wrapper) / AGP 8.7.0 / Kotlin 2.4.0, compileSdk 35
- Compose: compiled by `org.jetbrains.kotlin.plugin.compose` (not `composeOptions`)
- Internal deps: `:android-lib`, `:packages:core-*`, `:packages:fake`

---

## Requirements

- JDK 17
- Android SDK (ANDROID_HOME, platform-tools)
- Provide a `local.properties` file at the repo root with:

```
CLIENT_ID=xxxx
CLIENT_SECRET=yyyy
```

These values are read via `settings.gradle.kts` and exposed to the build.

---

## Build & run

From the monorepo root:

```bash
./gradlew :apps:android-sample:assembleDebug
./gradlew :apps:android-sample:installDebug
./gradlew :apps:android-sample:connectedAndroidTest
```

Opening directly in Android Studio is also supported.

---

## Structure

```
apps/android-sample/
├─ build.gradle.kts
├─ proguard-rules.pro
└─ src/
   ├─ main/                      # code, resources, manifest
   └─ androidTest/               # instrumented tests (Hilt Test Runner)
```

---

## Tests

```bash
./gradlew :apps:android-sample:test
./gradlew :apps:android-sample:connectedAndroidTest
```

---

## References

- See the root README for general setup, Metro, Yarn, and conventions.
