plugins {
    id("java-library")
    alias(libs.plugins.jetbrains.kotlin.jvm)
    alias(libs.plugins.kotlinx.serialization)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit)
}

tasks.register<JavaExec>("compressFonts") {
    group = "font"
    description = "Downloads missing Google Fonts, fetches designer info, creates compressed_fonts.zip, and updates fonts.json"
    mainClass.set("io.github.nndwn.getgooglefont.compressit.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
}


