// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models

import kotlinx.serialization.Serializable
import java.time.Instant

// Marker interface for streaming task results
@Serializable
sealed class TaskStreamingResult

// Core Objects
@Serializable
data class Task(
    val id: String,
    val sessionId: String? = null,
    val status: TaskStatus,
    val history: List<Message>? = null,
    val artifacts: List<Artifact>? = null,
    val metadata: Map<String, String> = emptyMap()
)

@Serializable
data class TaskStatus(
    val state: TaskState,
    val message: Message? = null,
    val timestamp: String = Instant.now().toString() // TODO
)

@Serializable
data class TaskStatusUpdateEvent(
    val id: String,
    val status: TaskStatus,
    val final: Boolean,
    val metadata: Map<String, String>? = null
) : TaskStreamingResult()

@Serializable
data class TaskArtifactUpdateEvent(
    val id: String,
    val artifact: Artifact,
    val metadata: Map<String, String>? = null
) : TaskStreamingResult()

@Serializable
data class TaskSendParams(
    val id: String,
    val sessionId: String? = null,
    val message: Message,
    val historyLength: Int? = null,
    val pushNotification: PushNotificationConfig? = null,
    val metadata: Map<String, String>? = null
)

@Serializable
enum class TaskState {
    submitted,
    working,
    `input-required`,
    completed,
    canceled,
    failed,
    unknown
}
