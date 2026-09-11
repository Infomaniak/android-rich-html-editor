plugins {
    `kotlin-dsl`
}

repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
}

gradlePlugin {
    plugins {
        register("infomaniak.publishPlugin") {
            id = "infomaniak.publishPlugin"
            implementationClass = "com.infomaniak.richhtmleditor.buildlogic.publish.PublishPlugin"
        }
    }
}
