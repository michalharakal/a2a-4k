// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.ktor.client.*
import io.ktor.client.engine.winhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * Windows implementation of HTTP client factory using WinHTTP engine.
 * WinHTTP engine provides good performance on Windows systems using native APIs.
 */
actual fun createHttpClient(): HttpClient = HttpClient(WinHttp) {
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