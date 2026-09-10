plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

group = "org.vander.core"
version = rootProject.findProperty("VERSION_NAME") ?: "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(libs.kotlinx.serialization.json)
}
