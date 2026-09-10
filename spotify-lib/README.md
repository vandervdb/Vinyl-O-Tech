# 📦 android-lib

Shared Android/Kotlin module for the monorepo. It exposes reusable components (DI, logging, Spotify SDK integrations,
etc.) consumed by apps and other modules.

---

## Overview

- Language: Kotlin (JDK 17)
- Build: Gradle 8.12 (wrapper) / AGP 8.7.0 / Kotlin 2.4.0, compileSdk 35
- Compose: compiled by `org.jetbrains.kotlin.plugin.compose` (not `composeOptions`)
- Target: Android (Compose enabled if needed)
- Consumers: apps/android-sample, other Kotlin modules

---

## Requirements

- JDK 17
- Android SDK installed (environment variables configured)
- Repository Gradle Wrapper

---

## Installation & build

From the monorepo root:

```bash
./gradlew :android-lib:assemble
```

To use it from another Gradle module:

```kotlin
implementation(project(":android-lib"))
```

---

## Structure

```
android-lib/
├─ build.gradle.kts
├─ libs/                         # Local AARs (e.g., Spotify SDK)
└─ src/
   ├─ main/
   └─ androidTest/
```

---

## Tests

```bash
./gradlew :android-lib:test
./gradlew :android-lib:connectedAndroidTest
```

---

## References

- See the root README for general setup and monorepo conventions.
