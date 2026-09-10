# 🧱 packages/core-dto

Kotlin DTOs (Data Transfer Objects), serializable via kotlinx.serialization, shared across modules.

---

## Overview

- Language: Kotlin (JDK 17)
- Serialization: kotlinx.serialization (JSON)
- Type: pure JVM module (no Android SDK required)

---

## Requirements

- JDK 17
- Repository Gradle Wrapper

---

## Installation & build

From the monorepo root:

```bash
./gradlew :packages:core-dto:build
```

Add as dependency:

```kotlin
implementation(project(":packages:core-dto"))
```

---

## Structure

```
packages/core-dto/
├─ build.gradle.kts
└─ src/
   └─ main/
```

---

## Tests

```bash
./gradlew :packages:core-dto:test
```

---

## References

- See the root README for conventions and global tooling.
