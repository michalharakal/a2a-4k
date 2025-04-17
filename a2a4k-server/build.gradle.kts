// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(project(":a2a4k-models"))
    implementation(libs.ktor.server.core.jvm)
    implementation(libs.ktor.server.netty.jvm)
    implementation(libs.ktor.server.sse)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.server.sse)
    implementation(libs.slf4j.api)

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.2")
    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
    testImplementation(libs.ktor.client.core)
    testImplementation(libs.ktor.client.cio.jvm)
    testImplementation(libs.ktor.serialization.kotlinx.json)
    testImplementation(libs.ktor.client.content.negotiation)
    testImplementation("io.mockk:mockk:1.13.10")
}
