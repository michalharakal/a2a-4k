// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models

import kotlinx.serialization.Serializable


@Serializable
data class Artifact(
    val name: String? = null,
    val description: String? = null,
    val parts: List<Part>,
    val metadata: Map<String, String>? = null,
    val index: Int,
    val append: Boolean? = null,
    val lastChunk: Boolean? = null
)

@Serializable
data class Message(
    val role: String, // "user" | "agent"
    val parts: List<Part>,
    val metadata: Map<String, String>? = null
)

@Serializable
sealed class Part {
    abstract val metadata: Map<String, String>
}

@Serializable
data class TextPart(
    val text: String,
    override val metadata: Map<String, String>
) : Part() {
    val type: String = "text"
}

@Serializable
data class FilePart(
    val file: FileData,
    override val metadata: Map<String, String>
) : Part() {
    val type: String = "file"
}

@Serializable
data class DataPart(
    val data: Map<String, String>,
    override val metadata: Map<String, String>
) : Part() {
    val type: String = "data"
}

@Serializable
data class FileData(
    val name: String? = null,
    val mimeType: String? = null,
    val bytes: String? = null, // base64 encoded content
    val uri: String? = null
)

@Serializable
data class Authentication(
    val schemes: List<String>,
    val credentials: String? = null
)


@Serializable
data class Provider(
    val organization: String,
    val url: String
)

@Serializable
data class Capabilities(
    val streaming: Boolean? = null,
    val pushNotifications: Boolean? = null,
    val stateTransitionHistory: Boolean? = null
)



