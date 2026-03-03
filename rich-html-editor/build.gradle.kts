plugins {
    alias(libs.plugins.android.library)
    id("maven-publish")
}

val sharedMinSdk: Int by rootProject.extra
val sharedCompileSdk: Int by rootProject.extra
val javaVersion: JavaVersion by rootProject.extra

android {
    namespace = "com.infomaniak.lib.richhtmleditor"
    compileSdk = sharedCompileSdk

    defaultConfig {
        minSdk = sharedMinSdk

        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components.findByName("release"))
                groupId = "com.github"
                artifactId = "android-rich-html-editor"
                version = "0.1.0"
            }
        }
    }
}
