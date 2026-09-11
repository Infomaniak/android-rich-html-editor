import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.metalava)
    alias(libs.plugins.publish)
    alias(libs.plugins.nmcp)
}

val sharedMinSdk = rootProject.extra["sharedMinSdk"] as Int
val sharedCompileSdk = rootProject.extra["sharedCompileSdk"] as Int
val javaVersion = rootProject.extra["javaVersion"] as JavaVersion

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
    namespace = "com.infomaniak.lib.richhtmleditor"
    compileSdk = sharedCompileSdk

    defaultConfig {
        minSdk = sharedMinSdk
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(javaVersion.toString())
    }
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.androidx.core.ktx)
}

metalava {
    filename = "metalavaApi/api.txt"
    enforceCheck = false
}
