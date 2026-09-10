# 🎨 packages/core-ui

Shared UI components in Kotlin/Compose for Android apps in the monorepo.

---

## Overview

- Language: Kotlin (JDK 17)
- UI: Jetpack Compose
- Type: Android Library (Compose enabled)

---

## Requirements

- JDK 17
- Android SDK

---

## Installation & build

From the monorepo root:

```bash
./gradlew :packages:core-ui:assemble
```

Add as dependency:

```kotlin
implementation(project(":packages:core-ui"))
```

---

## Structure

```
packages/core-ui/
├─ build.gradle.kts
├─ consumer-rules.pro
└─ src/
   └─ main/                      # Compose components
```

---

## Tests

```bash
./gradlew :packages:core-ui:test
```

---

## References

- See the root README for global setup.
