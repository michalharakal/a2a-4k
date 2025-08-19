// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.ktor.client.*
import io.ktor.client.engine.js.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.http.*

/**
 * JavaScript implementation of HTTP client factory using JS engine.
 * This engine uses the browser's fetch API or Node.js http module
 * depending on the JavaScript environment.
 */
actual fun createHttpClient(): HttpClient = HttpClient(Js) {
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