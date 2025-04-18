// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(project(":a2a4k-models"))
    implementation(project(":a2a4k-client"))
    implementation(project(":a2a4k-server-ktor"))
    implementation(libs.ktor.client.core)
    implementation(rootProject.libs.kotlinx.coroutines.slf4j)
    implementation(rootProject.libs.kotlinx.coroutines.jdk8)
    implementation(rootProject.libs.kotlinx.coroutines.reactor)
    implementation(rootProject.libs.kotlinx.serialization.json)
}
