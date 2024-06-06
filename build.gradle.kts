buildscript {
    extra.apply {
        set("sharedMinSdk", 24)
        set("sharedCompileSdk", 34)
        set("javaVersion", JavaVersion.VERSION_17)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
}
