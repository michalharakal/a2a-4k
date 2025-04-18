// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models

import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

/**
 * Helper function to convert a string to a Message object with role "user".
 */
fun String.toUserMessage() = Message(
    role = "user",
    parts = listOf(TextPart(text = this)),
)

/**
 * Converter class for deserializing JSON strings to A2ARequest objects.
 */
val a2aJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    coerceInputValues = true
    explicitNulls = false
    serializersModule = SerializersModule {
        polymorphicDefaultDeserializer(JsonRpcRequest::class) { UnknownMethodRequest.serializer() }
    }
}

fun String.toJsonRpcRequest() = a2aJson.decodeFromString<JsonRpcRequest>(this)
fun String.toJsonRpcResponse() = a2aJson.decodeFromString<JsonRpcResponse>(this)
fun JsonRpcRequest.toJson() = a2aJson.encodeToString(JsonRpcRequest.serializer(), this)
fun JsonRpcResponse.toJson() = a2aJson.encodeToString(JsonRpcResponse.serializer(), this)
fun AgentCard.toJson() = a2aJson.encodeToString(AgentCard.serializer(), this)
fun Task.toJson() = a2aJson.encodeToString(Task.serializer(), this)
