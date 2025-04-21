// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import kotlinx.coroutines.runBlocking
import org.a2a4k.models.Artifact
import org.a2a4k.models.Message
import org.a2a4k.models.Task
import org.a2a4k.models.TaskState
import org.a2a4k.models.TaskStatus
import org.a2a4k.models.TextPart

/**
 * A simple TaskHandler that calls your Agent.
 */
class BasicTaskHandler(private val completer: suspend (Message) -> String) : TaskHandler {

    override fun handle(task: Task): Task {
        // Extract the message from the task
        val message = task.history?.last() ?: error("Message was not found!")

        // Call your Agent with the message
        val response = runBlocking { completer(message) }

        // Create a response Artifact
        val responseArtifact = Artifact(
            name = "agent-response",
            parts = listOf(TextPart(text = response)),
        )

        // Update the task status
        val updatedStatus = TaskStatus(state = TaskState.COMPLETED)

        // Return the updated task
        return task.copy(status = updatedStatus, artifacts = listOf(responseArtifact))
    }
}

/**
 * Helper function to get message content.
 */
fun Message.content() = (this.parts.firstOrNull() as TextPart).text
