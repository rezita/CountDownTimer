import com.android.build.api.dsl.androidLibrary
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.vanniktech.mavenPublish)
}

group = "com.github.rezita"
version = "1.0.0"

kotlin {
    jvm()
    androidLibrary {
        namespace = "com.github.rezita.countdowntimer"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        withJava() // enable java compilation support
        withHostTestBuilder {}.configure {}
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }

        compilations.configureEach {
            compilerOptions.configure {
                jvmTarget.set(
                    JvmTarget.JVM_11
                )
            }
        }
    }
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    linuxX64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}

mavenPublishing {
    // Only sign if we have the keys (makes it optional)
    if (project.hasProperty("signingInMemoryKey")) {
        signAllPublications()
    }

    coordinates(group.toString(), "countdowntimer", version.toString())

    pom {
        name = "Count Down Timer"
        description = "Count Down Timer for KMP (Android and iOS)"
        inceptionYear = "2024"
        url = "https://github.com/rezita/CountDownTimer"
        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "rezita"
                name = "Zita Reiner"
                url = "https://github.com/rezita"
            }
        }
        scm {
            url = "https://github.com/rezita/CountDownTimer"
            connection = "scm:git:git://github.com/rezita/CountDownTimer.git"
            developerConnection = "scm:git:ssh://github.com/rezita/CountDownTimer.git"
        }
    }
}

// Configure GitHub Packages repository
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/rezita/CountDownTimer")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
