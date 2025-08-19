// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

kotlin {
    jvm()
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(project(":a2a4k-models"))
                implementation(project(":a2a4k-client"))
                implementation(project(":a2a4k-server-ktor"))
                implementation(project(":a2a4k-server-arc"))
                implementation(libs.ktor.client.core)
                implementation(libs.kotlinx.coroutines.slf4j)
                implementation(libs.kotlinx.coroutines.jdk8)
                implementation(libs.kotlinx.coroutines.reactor)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(libs.langchain4j.openai)
                implementation(libs.arc.agents)
                implementation(libs.arc.azure.client)
                implementation(libs.slf4j.jdk14)
            }
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}
