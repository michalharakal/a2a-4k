// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Creates a platform-specific HTTP client with optimized configuration for each target.
 * This function is implemented using expect/actual pattern to provide platform-specific
 * HTTP engines and configurations.
 *
 * @return A configured HttpClient optimized for the current platform
 */
expect fun createHttpClient(): HttpClient