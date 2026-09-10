# 📝 packages/core-security

Shared Kotlin logger based on Kermit, intended for Android/JVM modules in the monorepo.

---

## Overview



---

## Requirements

- JDK 17
- Android SDK (for Android target)

---

## Installation & build

From the monorepo root:

```bash
./gradlew :packages:core-security:assemble
```

Add as dependency:

```kotlin
api(project(":packages:core-security"))
```

---

## Structure

```
packages/core-security/
├─ build.gradle.kts
├─ consumer-rules.pro
└─ src/
   └─ main/
```

---

## Tests

```bash
./gradlew :packages:core-security:test
```

---

## References

- See the root README for global vision and conventions.
