@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

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
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.datetime)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(libs.kotlin.test.junit5)
                implementation(libs.junit.jupiter.api)
                implementation(libs.junit.jupiter.engine)
            }
        }
    }
}
