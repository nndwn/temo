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
    implementation(libs.commons.compress)
    implementation(libs.xz)
    testImplementation(libs.junit)
}

tasks.withType<Test>().configureEach {
    maxHeapSize = "2g"
}

tasks.register<JavaExec>("compressFonts") {
    group = "font"
    description = "Downloads missing Google Fonts, writes downloaded TTF files + fonts.json + per-script font bundles (bundles.json). Optional: -Pzip, -Plzma, -PbundleVersion, -PbaseUrl"
    mainClass.set("io.github.nndwn.getgooglefont.compressit.MainKt")
    classpath = sourceSets["main"].runtimeClasspath
    maxHeapSize = "2g"

    val inputMdFile = file("${rootDir}/fonts.md")
    val rawOutputDir = file("${rootDir}/app/src/main/res/raw")
    val localFontDir = file("${rootDir}/app/src/main/res/font")
    val downloadedFontsDir = file("${rootDir}/build/downloaded-fonts")

    args(
        inputMdFile.absolutePath,
        rawOutputDir.absolutePath,
        localFontDir.absolutePath,
        downloadedFontsDir.absolutePath,
    )

    if (project.hasProperty("zip")) {
        args("--zip")
    }
    if (project.hasProperty("lzma")) {
        args("--lzma")
    }
    if (project.hasProperty("bundleVersion")) {
        args("--bundle-version", project.property("bundleVersion") as String)
    }
    if (project.hasProperty("baseUrl")) {
        args("--base-url", project.property("baseUrl") as String)
    }
}


