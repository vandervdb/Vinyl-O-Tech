import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    id("kotlin-kapt")
}

android {
    namespace = "org.vander.android.sample"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "org.vander.android.sample"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode =
            libs.versions.android.versionCode
                .get()
                .toInt()
        versionName =
            libs.versions.android.versionName
                .get()

        testInstrumentationRunner = "com.google.dagger.hilt.android.testing.HiltTestRunner"

        manifestPlaceholders["redirectSchemeName"] = "org-vander-androidapp"
        manifestPlaceholders["redirectHostName"] = "callback"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
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

    lint {
        disable += "NullSafeMutableLiveData"
        disable += "RememberInComposition"
        disable += "AutoboxingStateCreation"
    }
}

dependencies {

    implementation(project(":spotify-lib"))
    implementation(project(":core:domain"))
    implementation(project(":core:logger"))
    implementation(project(":core:ui"))
    implementation(project(":fake"))

    // --- Spotify SDK AARs
    implementation(files("../../android-lib/libs/spotify-app-remote-release-0.8.0.aar"))
    implementation(files("../../android-lib/libs/spotify-auth-release-2.1.0.aar"))

    // --- Required runtime deps for Spotify SDK mappers and logging
    implementation(libs.jackson.databind)
    implementation(libs.slf4j.android)

    // --- AndroidX - Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.compose)

    // --- Compose (via BOM)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.ui.text.google.fonts)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.ui.graphics)
    debugImplementation(libs.compose.ui.tooling)

    // Activity Compose
    implementation(libs.activity.compose)

    // Material 3 + Icons + Adaptive
    implementation(libs.material)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons.ext)
    implementation(libs.androidx.material3.window.size)

    // --- Coil
    implementation(libs.coil.compose)

    // --- Gson
    implementation(libs.gson)

    // --- Hilt
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    kapt(libs.hilt.android)
    kapt(libs.androidx.hilt.compiler) //  @HiltViewModel
    kapt(libs.kotlin.metadata.jvm)
    implementation(libs.hilt.navigation.compose)

    // --- Tests
    testImplementation(libs.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)

    // Hilt (androidTest)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)
    kaptAndroidTest(libs.kotlin.metadata.jvm)

    //  Hilt Unit tests
    testImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    kaptTest(libs.kotlin.metadata.jvm)
}
