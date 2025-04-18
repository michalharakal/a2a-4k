// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.a2a4k.models.AgentCard
import org.a2a4k.models.Artifact
import org.a2a4k.models.Capabilities
import org.a2a4k.models.Message
import org.a2a4k.models.Task
import org.a2a4k.models.TaskState
import org.a2a4k.models.TaskStatus
import org.a2a4k.models.TextPart
import org.a2a4k.models.toUserMessage

// Custom TaskHandler that calls your Agent
class MyAgentTaskHandler : TaskHandler {
    override fun handle(task: Task): Task {
        // Extract the message from the task
        val message = task.history?.lastOrNull()

        // Call your Agent with the message
        val response = callYourAgent(message)

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

    private fun callYourAgent(message: Message?): String {
        // Implement your Agent call here
        // This could be an API call to an LLM, a custom AI service, etc.
        return "Response from your Agent"
    }
}

fun main() = runBlocking {
// Create your TaskHandler
    val taskHandler = MyAgentTaskHandler()

// Create a TaskManager with your TaskHandler
    val taskManager = BasicTaskManager(taskHandler)

// Define your Agent's capabilities
    val capabilities = Capabilities(
        streaming = true,
        pushNotifications = true,
        stateTransitionHistory = true,
    )

// Create an AgentCard for your Agent
    val agentCard = AgentCard(
        name = "My Agent",
        description = "A custom A2A Agent",
        url = "https://example.com",
        version = "1.0.0",
        capabilities = capabilities,
        defaultInputModes = listOf("text"),
        defaultOutputModes = listOf("text"),
        skills = listOf(/* Define your Agent's skills here */),
    )

    // Create and start the A2A Server
    val server = A2AServer(
        host = "0.0.0.0",
        port = 5001,
        endpoint = "/",
        agentCard = agentCard,
        taskManager = taskManager,
    )

    // Start the server
    server.start(wait = false)

    delay(1_000)

    val client = A2AClient(baseUrl = "http://localhost:5001")

    try {
        // Get the Agent's metadata
        val agentCard = client.getAgentCard()
        println("Connected to Agent: ${agentCard.name}")

        // Create a message to send to the Agent
        val message = "Hello, Agent!".toUserMessage()

        // Send a task to the Agent
        val response = client.sendTask(
            taskId = "task-123",
            sessionId = "session-456",
            message = message,
        )

        // Process the response
        if (response.result != null) {
            println("Agent response: $response")
        } else {
            println("Error: ${response.error?.message}")
        }
    } finally {
        // Close the client
        client.close()
    }
}
