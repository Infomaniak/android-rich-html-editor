buildscript {
    // The version is always provided explicitly via the "libVersion" Gradle property (used by
    // both the snapshot and release publishing workflows, which resolve it via auto-bump). Falls
    // back to "unspecified" for local builds/tests that don't need a real version.
    extra.apply {
        set("sharedMinSdk", 24)
        set("sharedCompileSdk", 37)
        set("javaVersion", JavaVersion.VERSION_17)
        set("libGroupId", "com.infomaniak.richhtmleditor")
        set("libVersionName", project.findProperty("libVersion") as String? ?: "unspecified")
        set("libArtifactId", "android-rich-html-editor")
    }

    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    id("maven-publish")
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.nmcp.aggregation)
}

// Aggregates the published rich-html-editor module into a single Maven Central deployment.
nmcpAggregation {
    centralPortal {
        username = providers.gradleProperty("ossrhUsername")
            .orElse(providers.environmentVariable("ossrhUsername"))
            .orNull
        password = providers.gradleProperty("ossrhPassword")
            .orElse(providers.environmentVariable("ossrhPassword"))
            .orNull
        publishingType = "AUTOMATIC"
    }
}

dependencies {
    nmcpAggregation(project(":rich-html-editor"))
}
