plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

group = "org.vander.core"
version = rootProject.findProperty("VERSION_NAME") ?: "0.1.0-SNAPSHOT"

android {
    namespace = "org.vander.core.ui"

    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()

        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = false
        compose = true
    }
}

kotlin {
    jvmToolchain(17)

    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    api(libs.kotlinx.coroutines.core)

    api(project(":core:domain"))
    implementation(project(":core:logger"))

    // Compose runtime is required by the Compose compiler plugin
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.runtime)

    implementation(libs.androidx.core.ktx)
}
