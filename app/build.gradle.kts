import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktlint)
}

detekt {
    config.setFrom(file("$rootDir/config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

// apply(plugin = "io.github.takahirom.roborazzi")
// Roborazzi is causing issues with the current AGP version/setup.
// Skipping plugin application for now but keeping dependencies.

val isRunningLocal =
    System
        .getenv("CI")
        .isNullOrEmpty()
val localProperties =
    if (isRunningLocal) {
        File(rootDir, "local.properties").inputStream().use {
            Properties().apply { load(it) }
        }
    } else {
        Properties()
    }

val mapboxToken: String =
    System.getenv("MAPBOX_TOKEN") ?: localProperties.getProperty("mapboxToken").orEmpty()

val sentryDsn: String =
    System.getenv("SENTRY_DSN") ?: localProperties.getProperty("SENTRY_DSN").orEmpty()

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

android {
    namespace = "com.boa.test.city.seeker"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.boa.test.city.seeker"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "CITIES_URL",
            "\"https://gist.githubusercontent.com/hernan-uala/dce8843a8edbe0b0018b32e137bc2b3a/raw/0996accf70cb0ca0e16f9a99e0ee185fafca7af1/\"",
        )
        buildConfigField(
            "String",
            "MAPBOX_TOKEN",
            "\"$mapboxToken\"",
        )
        // resValue("string", "mapbox_access_token", mapboxToken)
        buildConfigField(
            "String",
            "SENTRY_DSN",
            "\"$sentryDsn\"",
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
    //noinspection WrongGradleMethod
    composeCompiler {
        reportsDestination.set(layout.buildDirectory.dir("compose-reports"))
        metricsDestination.set(layout.buildDirectory.dir("compose-metrics"))
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    packaging {
        resources {
            excludes +=
                listOf(
                    "META-INF/LICENSE.md",
                    "META-INF/LICENSE-notice.md",
                    "META-INF/LICENSE.txt",
                    "META-INF/NOTICE.txt",
                    "META-INF/DEPENDENCIES",
                    "META-INF/ASL2.0",
                    "META-INF/AL2.0",
                    "META-INF/LGPL2.1",
                )
        }
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

dependencies {
    implementation(libs.jakewharton.timber)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.profileinstaller)

    // DI - Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.compose)
    implementation(libs.androidx.lifecycle.runtime.compose.android)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    ksp(libs.hilt.compiler)
    ksp(libs.kotlin.metadata.jvm)

    // Coroutines
    implementation(libs.jetbrains.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // UI
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.paging.runtime.ktx)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.concurrent.futures)
    implementation(libs.concurrent.futures.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.lottie.android)
    implementation(libs.mapbox)
    implementation(libs.mapbox.compose)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    implementation(libs.coil.compose)
    implementation(libs.androidx.ui.tooling.preview)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    // Sentry
    implementation(libs.sentry.android)
    implementation(libs.sentry.okhttp)
    implementation(libs.sentry.android.timber)

    // Quality Tools
    debugImplementation(libs.leakcanary)
    debugImplementation(libs.chucker.debug)
    releaseImplementation(libs.chucker.release)

    // Data
    implementation(libs.datastore.android)
    implementation(libs.retrofit.gson)
    implementation(libs.retrofit.android)
    implementation(libs.okhttp.android)
    implementation(libs.okhttp.interceptor)
    implementation(libs.android.gson)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.arch.core.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debugging
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
