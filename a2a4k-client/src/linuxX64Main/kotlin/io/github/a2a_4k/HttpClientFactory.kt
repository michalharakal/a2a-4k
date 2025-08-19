// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.ktor.client.*
import io.ktor.client.engine.curl.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Linux implementation of HTTP client factory using Curl engine.
 * Curl engine provides good performance on Linux systems.
 */
actual fun createHttpClient(): HttpClient = HttpClient(Curl) {
    defaultRequest {
        contentType(ContentType.Application.Json)
        header("Accept", ContentType.Application.Json.toString())
    }
    install(SSE) {
        maxReconnectionAttempts = 5
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 30000
        connectTimeoutMillis = 15000
        socketTimeoutMillis = 60000
    }
}