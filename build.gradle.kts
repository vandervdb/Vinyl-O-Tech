import com.diffplug.gradle.spotless.SpotlessTask
import com.diffplug.spotless.LineEnding

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.spotless)
    alias(libs.plugins.ktlint.gradle)
}

subprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jetbrains.kotlin:kotlin-stdlib:2.4.0")
            force("org.jetbrains.kotlin:kotlin-stdlib-common:2.4.0")
        }
    }
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        ignoreFailures.set(true)
    }
}

ktlint {
    verbose.set(true)
    outputToConsole.set(true)
    ignoreFailures.set(true) // ★★★★★ IMPORTANT ★★★★★
    android.set(false)
    filter {
        exclude("**/build/**")
        exclude("**/generated/**")
        exclude("**/node_modules/**")
    }
}

spotless {
    lineEndings = com.diffplug.spotless.LineEnding.UNIX

    kotlin {

        target(
            fileTree("android-lib") { include("**/*.kt") },
            fileTree("apps/android-sample") { include("**/*.kt") },
            fileTree("apps/RnSample/android/app") { include("**/*.kt") },
            fileTree("packages/android") { include("**/*.kt") },
            fileTree("rn-modules/RnModuleSpotifyClient/android/app") { include("**/*.kt") },
        )

        ktlint(libs.versions.ktlint.get())
    }

    kotlinGradle {
        target(
            files(
                "settings.gradle.kts",
                "build.gradle.kts",
                "android-lib/build.gradle.kts",
                "apps/android-sample/build.gradle.kts",
                "packages/android/core-security/build.gradle.kts",
                "packages/android/core-domain/build.gradle.kts",
                "packages/android/core-dto/build.gradle.kts",
                "packages/android/core-logger/build.gradle.kts",
                "packages/android/core-ui/build.gradle.kts",
                "packages/android/fake/build.gradle.kts",
            ),
        )

        ktlint(libs.versions.ktlint.get())
    }
}

// Désactive le configuration cache uniquement pour Spotless (évite l'état "stale" récurrent)
tasks.withType<SpotlessTask>().configureEach {
    notCompatibleWithConfigurationCache(
        "Spotless utilise un cache JVM-local pouvant devenir stale avec le " +
            "configuration cache (issue diffplug/spotless#987).",
    )
}

abstract class CheckCatalogConsistencyTask : DefaultTask() {
    @get:InputFile
    abstract val tomlFile: RegularFileProperty

    /**
     * Build scripts scanned for `libs.versions.<key>.get()` accessors — a version key can be
     * consumed there without ever appearing as a `version.ref` inside the catalog.
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val gradleFiles: ConfigurableFileCollection

    @get:Input
    abstract val allowedUnusedVersionKeys: ListProperty<String>

    @TaskAction
    fun check() {
        println("✅ Checking Version Catalog Consistency...")

        val file = tomlFile.get().asFile
        if (!file.exists()) {
            throw GradleException("📛 Cannot find ${file.path} !")
        }

        val content = file.readText()

        val declaredVersions =
            Regex("""^\s*([a-zA-Z0-9_-]+)\s*=\s*["'][^"']+["']""", RegexOption.MULTILINE)
                .findAll(content.substringAfter("[versions]").substringBefore("["))
                .map { it.groupValues[1] }
                .toSet()

        val refsInCatalog =
            Regex("""version(?:\.ref)?\s*=\s*["']([a-zA-Z0-9_-]+)["']""")
                .findAll(content)
                .map { it.groupValues[1] }
                .toSet()

        val refsInBuildScripts =
            gradleFiles.files
                .flatMap { script ->
                    VERSION_ACCESSOR_REGEX
                        .findAll(script.readText())
                        .map { it.groupValues[1].removeSuffix(".get") }
                        .toList()
                }.toSet()

        val used =
            (refsInCatalog + allowedUnusedVersionKeys.get()).mapTo(mutableSetOf(), ::normaliseKey) +
                refsInBuildScripts

        val unused = declaredVersions.filterNot { normaliseKey(it) in used }

        if (unused.isEmpty()) {
            println("✅ Version Catalog is clean! 🎉")
            return
        }

        println("⚠️  Some versions are not applied in catalog:")
        unused.forEach { println("- $it") }
        throw GradleException("❌ Invalid version catalog: detected ${unused.size} unused version key(s).")
    }

    private companion object {
        /** Matches `libs.versions.foo.bar`, including when `.get()` sits on the next line. */
        val VERSION_ACCESSOR_REGEX = Regex("""\blibs\.versions\.([a-zA-Z0-9_.]+)""")

        /**
         * Gradle turns `-` and `_` in a catalog key into `.` in the generated accessor
         * (`android-compileSdk` → `libs.versions.android.compileSdk`), so both sides are
         * compared on that normalised form.
         */
        fun normaliseKey(key: String): String = key.replace('-', '.').replace('_', '.')
    }
}

tasks.register<CheckCatalogConsistencyTask>("checkCatalogConsistency") {
    group = "verification"
    description = "Checking whether all versions in libs.versions.toml are used."

    tomlFile.set(layout.projectDirectory.file("gradle/libs.versions.toml"))
    gradleFiles.from(
        layout.projectDirectory.asFileTree.matching {
            include("**/build.gradle.kts", "**/build.gradle", "**/settings.gradle.kts")
            exclude("**/build/**")
        },
    )

    // Keys that are genuinely unreferenced but must stay in the catalog. Empty on purpose:
    // `android-*Sdk`/`android-version*` used to live here only because the task could not see
    // `libs.versions.*.get()` accessors — it now does.
    allowedUnusedVersionKeys.set(emptyList())
}

abstract class CheckVersionHardcodedUsagesTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val gradleFiles: ConfigurableFileCollection

    @TaskAction
    fun check() {
        println("🔍 Looking for hard-coded dependencies inside dependencies { } blocks...")

        val badUsages =
            gradleFiles.files.sortedBy { it.path }.flatMap { file ->
                val lines = file.readText().lines()
                dependencyBlocks(lines).flatMap { block ->
                    block
                        .filter { isHardcodedDependency(lines[it]) }
                        .map { "${file.path}:${it + 1} → ${lines[it].trim()}" }
                }
            }

        if (badUsages.isEmpty()) {
            println("✅ All dependencies in dependencies { } use the Version Catalog.")
            return
        }

        println("⚠️  Hard-coded dependencies detected:")
        badUsages.forEach { println(it) }
        throw GradleException("❌ One or more dependencies do not use the Version Catalog (libs.*).")
    }

    private companion object {
        val DEPENDENCY_BLOCK_START_REGEX = Regex("""^\s*dependencies\s*\{""")

        /** `"group:artifact:version"` — three colon-separated segments between quotes. */
        val HARDCODED_GAV_REGEX = Regex("""["'][^"']+:[^"']+:[^"']+["']""")

        /**
         * Line ranges of every `dependencies { }` block, delimited by brace counting — a
         * non-greedy `.*?}` regex stops at the first nested closing brace instead, which both
         * truncates the block and makes reported line numbers block-relative.
         */
        fun dependencyBlocks(lines: List<String>): List<IntRange> {
            val blocks = mutableListOf<IntRange>()
            var start = -1
            var depth = 0

            lines.forEachIndexed { index, line ->
                if (start < 0 && !DEPENDENCY_BLOCK_START_REGEX.containsMatchIn(line)) return@forEachIndexed

                if (start < 0) {
                    start = index
                    depth = 0
                }
                depth += line.count { it == '{' } - line.count { it == '}' }
                if (depth > 0) return@forEachIndexed

                blocks += start..index
                start = -1
            }

            if (start >= 0) blocks += start..lines.lastIndex
            return blocks
        }

        fun isHardcodedDependency(line: String): Boolean {
            val code = line.trim()
            if (code.startsWith("//")) return false
            // `project(":a:b:c")` is a project accessor, not a catalog candidate
            if ("project(" in code) return false
            if ("libs." in code) return false
            return HARDCODED_GAV_REGEX.containsMatchIn(code)
        }
    }
}

tasks.register<CheckVersionHardcodedUsagesTask>("checkVersionHardcodedUsages") {
    group = "verification"
    description = "Verify all dependencies use the Gradle Version Catalog (libs.*)."

    gradleFiles.from(
        layout.projectDirectory.asFileTree.matching {
            include("**/build.gradle.kts", "**/build.gradle")
            exclude("**/build/**")
        },
    )
}
