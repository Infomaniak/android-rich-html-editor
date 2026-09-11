package com.infomaniak.richhtmleditor.buildlogic.publish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.tasks.AbstractPublishToMaven
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType
import org.gradle.plugins.signing.Sign
import org.gradle.plugins.signing.SigningExtension
import org.gradle.plugins.signing.SigningPlugin

class PublishPlugin : Plugin<Project> {

    override fun apply(target: Project) = with(target) {
        plugins.apply(SigningPlugin::class.java)
        plugins.apply("maven-publish")

        val libGroupId = rootProject.extra["libGroupId"] as String
        val libVersionName = rootProject.extra["libVersionName"] as String
        val libArtifactId = rootProject.extra["libArtifactId"] as String

        group = libGroupId
        version = libVersionName

        afterEvaluate {
            extensions.configure<PublishingExtension> {
                publications {
                    create<MavenPublication>("release") {
                        from(components.getByName("release"))
                        groupId = libGroupId
                        artifactId = libArtifactId
                        version = libVersionName
                        pom {
                            name.set("android-rich-html-editor")
                            description.set("Android library to display HTML and easily modify it on the fly, using a WebView")
                            url.set("https://github.com/Infomaniak/android-rich-html-editor")
                            licenses {
                                license {
                                    name.set("Apache-2.0")
                                    url.set("https://www.apache.org/licenses/LICENSE-2.0")
                                }
                            }
                            scm {
                                connection.set("scm:git:git://github.com/Infomaniak/android-rich-html-editor.git")
                                developerConnection.set("scm:git:ssh://github.com/Infomaniak/android-rich-html-editor.git")
                                url.set("https://github.com/Infomaniak/android-rich-html-editor")
                            }
                            developers {
                                developer {
                                    id.set("Infomaniak")
                                    name.set("Infomaniak Development Team")
                                    email.set("mobile+libraries@infomaniak-dev.ch")
                                    url.set("https://www.infomaniak.com/")
                                }
                            }
                        }
                    }
                }

                repositories {
                    maven {
                        name = "reposilite"
                        url = uri(
                            if (libVersionName.endsWith("SNAPSHOT")) {
                                "https://maven.infomaniak.app/snapshots"
                            } else {
                                "https://maven.infomaniak.app/releases"
                            }
                        )
                        credentials {
                            username = getPropertyValue("reposiliteUsername")
                            password = getPropertyValue("reposilitePassword")
                        }
                    }
                }
            }

            extensions.configure<SigningExtension> {
                val keyId: String = getPropertyValue("GPG_key_id") ?: return@configure
                val ringFile: String = getPropertyValue("GPG_private_key")?.replace('#', '\n') ?: return@configure
                val password: String = getPropertyValue("GPG_private_password") ?: return@configure

                isRequired = true
                useInMemoryPgpKeys(keyId, ringFile, password)
                sign(project.extensions.getByType<PublishingExtension>().publications)

                // Workaround for a Gradle bug, the issue is still open.
                // https://github.com/gradle/gradle/issues/26091#issuecomment-1722947958
                tasks.withType<AbstractPublishToMaven>().configureEach {
                    val signingTasks = tasks.withType<Sign>()
                    mustRunAfter(signingTasks)
                }
            }
        }
    }

    private fun Project.getPropertyValue(propertyName: String): String? {
        if (project.hasProperty(propertyName)) return project.property(propertyName) as String
        return System.getenv(propertyName)
    }

}
