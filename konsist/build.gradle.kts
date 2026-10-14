plugins {
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    testImplementation(libs.junit4)
    testImplementation(libs.konsist)
}

tasks.test {
    // Konsist lit les sources des autres modules à l'exécution ;
    // sans ça, Gradle ignore leurs changements et peut sauter le test.
    inputs
        .files(
            fileTree(rootDir) {
                include("**/src/**/*.kt")
                exclude("**/build/**")
            },
        ).withPropertyName("projectSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
}
