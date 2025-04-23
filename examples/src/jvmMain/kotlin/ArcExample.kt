// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.a2a4k.models.AgentCard
import org.a2a4k.models.Capabilities
import org.a2a4k.models.toUserMessage
import org.eclipse.lmos.arc.agents.agent.ask
import org.eclipse.lmos.arc.agents.agents
import org.eclipse.lmos.arc.agents.getChatAgent
import org.eclipse.lmos.arc.core.getOrThrow

/**
 * Example using the Kotlin AI Framework Ard, see https://eclipse.dev/lmos/arc2/.
 */
fun main() = runBlocking {
    // Only required if OPENAI_API_KEY is not set as an environment variable.
    // System.setProperty("OPENAI_API_KEY", "****")

    // Create your Agent
    val agents = agents {
        agent {
            name = "MyAgent"
            model { "gpt-4o-mini" }
            prompt {
                """ You are a helpful assistant. Greet the user with "Hello, from A2A"! """
            }
        }
    }

// Create your TaskHandler
    val taskHandler = BasicTaskHandler({ message ->
        agents.getChatAgent("MyAgent").ask(message.content()).getOrThrow()
    })

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
        skills = listOf(),
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

    // Wait for the server to start
    delay(1_000)

    val client = A2AClient(baseUrl = "http://localhost:5001")

    try {
        // Get the Agent's metadata
        val agentCard = client.getAgentCard()
        println("Connected to Agent: ${agentCard.name}")

        // Create a message to send to the Agent
        val message = "Hello, Agent!".toUserMessage()

        // Send a task to the Agent
        val response = client.sendTask(taskId = "task-123", sessionId = "session-456", message = message)

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
