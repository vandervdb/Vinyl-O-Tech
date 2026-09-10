# 🧪 packages/fake

Shared fake implementations and test data to facilitate prototyping and testing of Android/Kotlin modules.

---

## Overview

- Language: Kotlin (JDK 17)
- Type: Android Library
- Usage: used by `:apps:android-sample` and other modules during test/demo phases

---

## Requirements

- JDK 17
- Android SDK

---

## Installation & build

From the monorepo root:

```bash
./gradlew :packages:fake:assemble
```

Add as dependency (demo/test):

```kotlin
implementation(project(":packages:fake"))
```

---

## Structure

```
packages/fake/
├─ build.gradle.kts
├─ consumer-rules.pro
└─ src/
   └─ main/
```

---

## Tests

```bash
./gradlew :packages:fake:test
```

---

## References

- See the root README for conventions and testing practices.
