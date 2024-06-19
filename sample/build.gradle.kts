plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

val sharedMinSdk: Int by rootProject.extra
val sharedCompileSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

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
    kotlinOptions {
        jvmTarget = javaVersion.toString()
    }
    buildFeatures {
        viewBinding = true
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
