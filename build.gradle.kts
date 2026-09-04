buildscript {
    extra.apply {
        set("sharedMinSdk", 24)
        set("sharedCompileSdk", 37)
        set("javaVersion", JavaVersion.VERSION_17)
    }

    dependencies {
        classpath(libs.kotlin.gradle.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}
