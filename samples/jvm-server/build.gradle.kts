// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

kotlin {
    jvm ()

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.kotlinx.coroutines.jdk8)

            implementation(project(":a2a4k-server-ktor"))
            implementation(project(":a2a4k-models"))
            implementation("org.slf4j:slf4j-simple:2.0.9")
        }
    }
}