// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.a2a4k.models.*
import java.util.UUID

/**
 * List of Exceptions that can be thrown by the A2A4K client.
 */

sealed class A2AException(message: String, ex: Exception? = null) : Exception(message, ex)

class ServerException(message: String, ex: Exception? = null) : A2AException(message, ex)