// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

kotlin {
    jvm()
    
    js(IR) {
        browser()
        nodejs()
    }
    
    wasmJs {
        browser()
    }
    
    // Apple platforms
    iosX64()
    iosArm64()
    iosSimulatorArm64()
    macosX64()
    macosArm64()
    
    // Linux
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":a2a4k-models"))
                implementation(libs.kotlinx.coroutines)
                implementation(libs.kotlinx.serialization.json)

                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.content.negotiation)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.kotlinx.coroutines.slf4j)
                implementation(libs.kotlinx.coroutines.jdk8)
                implementation(libs.kotlinx.coroutines.reactor)
                implementation(libs.ktor.client.cio.jvm)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(project(":a2a4k-server-ktor"))
                implementation(libs.kotlin.test.junit5)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
                implementation(libs.slf4j.jdk14)
            }
        }

        val jsMain by getting {
            dependencies {
                implementation(libs.ktor.client.js)
            }
        }

        val linuxX64Main by getting {
            dependencies {
                implementation(libs.ktor.client.curl)
            }
        }

        val iosX64Main by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosArm64Main by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val macosX64Main by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }

        val macosArm64Main by getting {
            dependencies {
                implementation(libs.ktor.client.darwin)
            }
        }
    }
}
