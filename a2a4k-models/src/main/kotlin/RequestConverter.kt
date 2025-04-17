// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models

import kotlinx.serialization.json.Json

/**
 * Converter class for deserializing JSON strings to A2ARequest objects.
 */
class RequestConverter {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Converts a JSON string to an A2ARequest object.
     *
     * @param jsonString The JSON string to convert
     * @return The deserialized A2ARequest object
     * @throws IllegalArgumentException if the method is not supported
     * @throws kotlinx.serialization.json.JsonDecodingException if the JSON is invalid
     */
    fun fromJson(jsonString: String): JsonRpcRequest {
        try {
            return json.decodeFromString<JsonRpcRequest>(jsonString)
        } catch (e: Exception) {
            throw IllegalArgumentException("Unexpected error while parsing A2ARequest: $jsonString", e)
        }
    }
}