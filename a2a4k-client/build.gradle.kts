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
    
    // Windows
    mingwX64()
    
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":a2a4k-models"))
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.content.negotiation)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
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
                implementation("org.jetbrains.kotlin:kotlin-test-junit5:2.1.10")
                implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
            }
        }

        val jsMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-js:3.1.3")
            }
        }

        val linuxX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-curl:3.1.3")
            }
        }

        val mingwX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-winhttp:3.1.3")
            }
        }

        val iosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.3")
            }
        }

        val iosArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.3")
            }
        }

        val iosSimulatorArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.3")
            }
        }

        val macosX64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.3")
            }
        }

        val macosArm64Main by getting {
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.1.3")
            }
        }
    }
}
