# 📝 packages/core-logger

Shared Kotlin logger based on Kermit, intended for Android/JVM modules in the monorepo.

---

## Overview

- Language: Kotlin (JDK 17)
- Platforms: Android/JVM
- Lib: co.touchlab:kermit
- Type: Android Library (no Compose)

---

## Requirements

- JDK 17
- Android SDK (for Android target)

---

## Installation & build

From the monorepo root:

```bash
./gradlew :packages:core-logger:assemble
```

Add as dependency:

```kotlin
api(project(":packages:core-logger"))
```

---

## Structure

```
packages/core-logger/
├─ build.gradle.kts
├─ consumer-rules.pro
└─ src/
   └─ main/
```

---

## Tests

To use core-logger in tests, use NoOpLogger or TestLogger implementation.

---

## References

- See the root README for global vision and conventions.
