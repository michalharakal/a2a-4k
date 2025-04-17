// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(project(":a2a4k-models"))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio.jvm)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
}
