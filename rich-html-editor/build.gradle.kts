plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.metalava)
    id("maven-publish")
}

val sharedMinSdk: Int by rootProject.extra
val sharedCompileSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    publishing {
        singleVariant("release") {
            withSourcesJar()
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
        jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.fromTarget(javaVersion.toString())
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

publishing {
    publications {
        create<MavenPublication>("release") {
            afterEvaluate {
                from(components["release"])
            }
            groupId = "com.github.infomaniak"
            artifactId = "android-rich-html-editor"
            version = "1.1.0"
        }
    }
}
