import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)

    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.play.publisher)
    id("io.github.nndwn.changelog-publish") version "0.3.2"
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

android {
    namespace = "com.nndwn.runtext"
    compileSdk = 37

    flavorDimensions.add("distribution")
    productFlavors {
        create("playstore") {
            dimension = "distribution"
        }
        create("foss") {
            dimension = "distribution"
            versionNameSuffix = "-foss"
        }
    }

    defaultConfig {
        applicationId = "com.nndwn.runtext"
        minSdk = 28
        targetSdk = 37
        versionCode = 19
        versionName = "1.5.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    

    signingConfigs {
        create("release") {
            val keyFile = localProperties.getProperty("KEY_FILE")
            if (keyFile != null) {
                if (keyFile.isEmpty()) return@create
                storeFile = rootProject.file(keyFile)
                storePassword = localProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = localProperties.getProperty("KEY_ALIAS")
                keyPassword = localProperties.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            // Keep the Play-only "Dependency metadata" APK Signing Block (ID 0x504B4453) out of the APK.
            // `fdroid scanner` flags it as "Found extra signing block", which fails F-Droid's `check apk`
            // job and therefore blocks reproducible builds for this app. The app bundle keeps its
            // default (`includeInBundle` stays true), so Play still receives the metadata.
            dependenciesInfo {
                includeInApk = false
            }
            val releaseConfig = signingConfigs.getByName("release")
            signingConfig = if (releaseConfig.storeFile?.exists() == true) {
                releaseConfig
            } else {
                signingConfigs.getByName("debug")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

}


play {
    val playAccountJsonFromEnv = System.getenv("PLAY_SERVICE_ACCOUNT_JSON")
    val playAccountFilePath = localProperties.getProperty("PLAY_SERVICE_ACCOUNT_FILE")
        ?: System.getenv("PLAY_SERVICE_ACCOUNT_FILE")

    when {
        !playAccountJsonFromEnv.isNullOrEmpty() -> {
            val tempFile = layout.buildDirectory.file("play-service-account.json").get().asFile
            if (!tempFile.exists()) {
                tempFile.parentFile.mkdirs()
                tempFile.writeText(playAccountJsonFromEnv)
            }
            serviceAccountCredentials.set(tempFile)
        }
        !playAccountFilePath.isNullOrEmpty() -> {
            serviceAccountCredentials.set(file(playAccountFilePath))
        }
        else -> {
            serviceAccountCredentials.set(rootProject.file("play-service-account.json"))
        }
    }

    // Upload straight to the production track (GPP's own default is `internal`).
    // Every release MUST bump `versionCode` above - Play rejects a version code that was ever used.
    track.set("production")
    defaultToAppBundles.set(true)
}

dependencies {

    "playstoreImplementation"(libs.app.update.ktx)
    "playstoreImplementation"(libs.billing.ktx)
    "playstoreImplementation"(libs.review.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3.window.size.class1)

    //AndroidX Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    //Navigation
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.navigation3.ui)

    //Datastore and Serization
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.kotlinx.serialization.json)

    //Media3 Video Export
    implementation(libs.androidx.media3.transformer)
    implementation(libs.androidx.media3.effect)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.muxer)
    //WorkManager
    implementation(libs.androidx.work.runtime.ktx)
    //DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    //Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.serialization.json)
    testImplementation(libs.kotlinx.coroutines.test)
    debugImplementation(libs.androidx.compose.ui.tooling)
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}