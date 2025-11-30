@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.2.21"
    id("org.jetbrains.dokka") version "2.0.0"

    `maven-publish`
    signing
}

val v = "1.0.0"
group = "xyz.calcugames"
version = "${if (project.hasProperty("snapshot")) "$v-SNAPSHOT" else v}${project.findProperty("suffix")?.toString()?.run { "-${this}" } ?: ""}"
description = "raylib Kotlin Multiplatform Bindings"

repositories {
    mavenCentral()
    mavenLocal()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

kotlin {
    configureSourceSets()
    applyDefaultHierarchyTemplate()
    withSourcesJar()

    linuxX64()
    macosX64()
    macosArm64()
    mingwX64()

    targets.filterIsInstance<KotlinNativeTarget>().forEach { target ->
        target.compilations.all {
            cinterops {
                val raylib by creating {
                    defFile(project.file("lib/raylib.def"))

                    includeDirs {
                        allHeaders(project.file("lib/raylib/src"))
                    }
                }
            }
        }
    }
}

fun KotlinMultiplatformExtension.configureSourceSets() {
    sourceSets
        .matching { it.name !in listOf("main", "test") }
        .all {
            val srcDir = if ("Test" in name) "test" else "main"
            val resourcesPrefix = if (name.endsWith("Test")) "test-" else ""
            val platform = when {
                (name.endsWith("Main") || name.endsWith("Test")) && "android" !in name -> name.dropLast(4)
                else -> name.substringBefore(name.first { it.isUpperCase() })
            }

            kotlin.srcDir("src/$platform/$srcDir")
            resources.srcDir("src/$platform/${resourcesPrefix}resources")

            languageSettings.apply {
                progressiveMode = true
            }
        }
}

signing {
    val signingKey: String? by project
    val signingPassword: String? by project

    if (signingKey != null && signingPassword != null)
        useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
}

publishing {
    publications {
        filterIsInstance<MavenPublication>().forEach {
            it.apply {
                pom {
                    name = "raylibkt"

                    licenses {
                        license {
                            name = "MIT License"
                            url = "https://opensource.org/licenses/MIT"
                        }
                    }

                    developers {
                        developer {
                            id = "gmitch215"
                            name = "Gregory Mitchell"
                            email = "me@gmitch215.xyz"
                        }
                    }

                    scm {
                        connection = "scm:git:git://github.com/CalculusGames/raylibkt.git"
                        developerConnection = "scm:git:ssh://github.com/CalculusGames/raylibkt.git"
                        url = "https://github.com/CalculusGames/raylibkt"
                    }
                }
            }
        }
    }

    repositories {
        maven {
            name = "CalculusGames"
            credentials {
                username = System.getenv("NEXUS_USERNAME")
                password = System.getenv("NEXUS_PASSWORD")
            }

            val releases = "https://repo.calcugames.xyz/repository/maven-releases/"
            val snapshots = "https://repo.calcugames.xyz/repository/maven-snapshots/"
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshots else releases)
        }

        if (!version.toString().endsWith("SNAPSHOT")) {
            maven {
                name = "GithubPackages"
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }

                url = uri("https://maven.pkg.github.com/CalculusGames/raylibkt")
            }
        }
    }
}