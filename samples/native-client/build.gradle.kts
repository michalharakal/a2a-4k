// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0


kotlin {
    kotlin {
        macosArm64("macosMain") {  // on macOS
            // linuxArm64("native") // on Linux
            // mingwX64("native")   // on Windows
            binaries {
                executable()
            }
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(project(":a2a4k-client"))
            implementation(project(":a2a4k-models"))
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kotlinx.serialization.json)
        }

        macosMain.dependencies {
            // Darwin/macOS specific HTTP client
            implementation(libs.ktor.client.darwin)
        }
/*
        linuxMain.dependencies {
            // Linux cURL-based HTTP client
            implementation(libs.ktor.client.curl)
        }

 */
    }

    // Make a native executable with a main() entry point
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        binaries {
            executable("client") {
                entryPoint = "main"
                // Optional: set output name
                baseName = "hello"
            }
        }
    }
}