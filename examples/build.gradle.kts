// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

dependencies {
    implementation(project(":a2a4k-models"))
    implementation(project(":a2a4k-client"))
    implementation(project(":a2a4k-server-ktor"))
    implementation(libs.ktor.client.core)
    implementation(libs.bundles.kotlinx)
}
