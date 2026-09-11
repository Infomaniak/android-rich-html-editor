buildscript {
    // The version is provided explicitly via the "libVersion" Gradle property (used by both the
    // snapshot and release publishing workflows, which resolve it via auto-bump). JitPack instead
    // exposes the requested Git tag/commit through the "VERSION" environment variable (it does not
    // set any Gradle property), so it's read as a second fallback to keep JitPack builds working.
    // Likewise, JitPack exposes the "GROUP" environment variable (normally "com.github.<owner>")
    // used to locate the published artifact locally; it must be honored as the publication's
    // groupId for JitPack builds, falling back to our own Reposilite group otherwise.
    // Both fall back to sensible defaults for local builds/tests that don't need a real version.
    extra.apply {
        set("sharedMinSdk", 24)
        set("sharedCompileSdk", 37)
        set("javaVersion", JavaVersion.VERSION_17)
        set("libGroupId", System.getenv("GROUP") ?: "com.infomaniak.richhtmleditor")
        set(
            "libVersionName",
            project.findProperty("libVersion") as String?
                ?: System.getenv("VERSION")
                ?: "unspecified",
        )
        set("libArtifactId", "android-rich-html-editor")
    }

    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}
