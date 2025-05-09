// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.github.a2a_4k.models.*

/**
 * List of Exceptions that can be thrown by the A2A4K client.
 */

sealed class A2AException(message: String, ex: Exception? = null) : Exception(message, ex)
class ServerException(message: String, ex: Exception? = null) : A2AException(message, ex)
