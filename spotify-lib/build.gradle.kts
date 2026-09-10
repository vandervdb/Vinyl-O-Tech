import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.dagger.hilt.android")
    id("kotlin-kapt")
}

// Keep androidTest classpath aligned with Kotlin 2.4.0 by forcing compatible versions
configurations {
    matching { it.name.contains("AndroidTest", ignoreCase = true) }.configureEach {
        resolutionStrategy {
            force(
                // Kotlin stdlib compatible with 2.4.x
                "org.jetbrains.kotlin:kotlin-stdlib:2.4.0",
                "org.jetbrains.kotlin:kotlin-stdlib-common:2.4.0",
            )
        }
    }
}

android {
    namespace = "org.vander.spotifyclient"
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

        testInstrumentationRunner = "org.vander.spotifyclient.HiltTestRunner"

        val spotifyClientSecret = gradle.extra["spotifyClientSecret"] as String
        val spotifyClientId = gradle.extra["spotifyClientId"] as String
        buildConfigField("String", "CLIENT_SECRET", "\"$spotifyClientSecret\"")
        buildConfigField("String", "CLIENT_ID", "\"$spotifyClientId\"")

        manifestPlaceholders["redirectSchemeName"] = "org-vander-androidapp"
        manifestPlaceholders["redirectHostName"] = "callback"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "META-INF/LICENSE.md"
            excludes += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {

    // --- Projects
    implementation(project(":core:domain"))
    implementation(project(":core:logger"))
    implementation(project(":core:dto"))
    implementation(project(":core:ui"))

    // --- KotlinX
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.fragment.ktx)

    // --- AndroidX Core
    implementation(libs.androidx.core.ktx)

    // --- Lifecycle
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.compose)

    // --- Navigation
    implementation(libs.navigation.compose)

    // --- Compose (via BOM)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)

    // Material 3 + Adaptive
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.ext)

    // Activity Compose
    implementation(libs.activity.compose)

    // --- Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.kotlin.metadata.jvm)
    // si nécessaire :
    // kapt(libs.androidx.hilt.compiler)

    // --- Spotify SDK (provided by consumer apps)
    // Use compileOnly here to avoid bundling local AARs into this AAR (AGP restriction)
    compileOnly(files("libs/spotify-app-remote-release-0.8.0.aar"))
    compileOnly(files("libs/spotify-auth-release-2.1.0.aar"))

    // --- AndroidX Utils
    implementation(libs.androidx.browser)

    // --- Ktor
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.serialization.kotlinx.json)

    // --- DataStore
    implementation(libs.datastore.preferences)

    // --- Gson
    implementation(libs.gson)

    // --- Coil (images)
    implementation(libs.coil.compose)

    // --- Tests
    testImplementation(libs.junit4)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.kotlinx.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    kaptTest(libs.kotlin.metadata.jvm)
    testImplementation(kotlin("test"))

    debugImplementation(libs.compose.ui.test.manifest)

    androidTestImplementation(libs.androidx.test.ext.junit)
    // Keep Kotlin stdlib on androidTest classpath aligned with the project Kotlin (2.4.0)
    androidTestImplementation(platform(libs.kotlin.bom))
    // Keep coroutines on a version compatible with Kotlin 1.9.25 for androidTest
    androidTestImplementation(platform(libs.kotlinx.coroutines.bom))
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.core)
    // Turbine for testing Flow/StateFlow emissions in androidTest
    androidTestImplementation(libs.kotlinx.turbine)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.mockk.android)

    // Hilt (androidTest)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    kaptAndroidTest(libs.kotlin.metadata.jvm)
}
