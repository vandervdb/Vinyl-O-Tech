# 🧩 packages/core-domain

Shared domain interfaces and models in Kotlin for Android modules in the monorepo.

---

## Overview

- Language: Kotlin (JDK 17)
- Type: Android Library module (no UI)
- Usage: dependency of modules such as `:packages:core-dto`, `:android-lib`, `:apps:android-sample`

---

## Requirements

- JDK 17
- Android SDK for compilation (via the repo Gradle Wrapper)

---

## Installation & build

From the monorepo root:

```bash
./gradlew :packages:core-domain:assemble
```

Add as dependency of a Gradle module:

```kotlin
implementation(project(":packages:core-domain"))
```

---

## Structure

```
packages/core-domain/
├─ build.gradle.kts
└─ src/
   └─ main/
```

---

## Tests

```bash
./gradlew :packages:core-domain:test
```

---

## References

- See the root README for build conventions and project organization.
