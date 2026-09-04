plugins {
    alias(libs.plugins.android.application)
}

val sharedMinSdk = rootProject.extra["sharedMinSdk"] as Int
val sharedCompileSdk = rootProject.extra["sharedCompileSdk"] as Int
val javaVersion = rootProject.extra["javaVersion"] as JavaVersion

android {
    namespace = "com.infomaniak.lib.richhtmleditor.sample"
    compileSdk = sharedCompileSdk

    defaultConfig {
        applicationId = "com.infomaniak.lib.richhtmleditor.sample"
        minSdk = sharedMinSdk
        targetSdk = sharedCompileSdk
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
    buildFeatures {
        viewBinding = true
    }
}

kotlin {
    compilerOptions {
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString())
    }
}

dependencies {
    implementation(project(":rich-html-editor"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}
