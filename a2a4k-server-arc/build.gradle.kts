// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(project(":a2a4k-server-ktor"))
                api(project(":a2a4k-models"))
                implementation(libs.kotlinx.coroutines.slf4j)
                implementation(libs.kotlinx.coroutines.jdk8)
                implementation(libs.kotlinx.coroutines.reactor)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.server.sse)
                implementation(libs.slf4j.api)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation("org.eclipse.lmos:arc-agents:0.139.0")
                implementation("org.eclipse.lmos:arc-azure-client:0.139.0")
            }
        }

        val jvmTest by getting {
            dependencies {

                // Test dependencies
                implementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
                implementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
                implementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.cio.jvm)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.content.negotiation)
                implementation("io.mockk:mockk:1.13.10")
            }
        }
    }
}
