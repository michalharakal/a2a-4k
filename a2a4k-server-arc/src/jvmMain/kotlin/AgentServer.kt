// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k.arc

import io.github.a2a_4k.A2AServer
import io.github.a2a_4k.BasicTaskManager
import io.github.a2a_4k.models.AgentCard
import io.github.a2a_4k.models.Capabilities
import io.github.a2a_4k.models.Skill
import org.eclipse.lmos.arc.agents.ArcAgents
import org.eclipse.lmos.arc.agents.ConversationAgent

/**
 * Starts an A2A server with an agent from the ArcAgents instance.
 *
 * This function configures and starts an A2A server that exposes the first agent
 * found in the ArcAgents instance as an A2A-compatible agent. It creates the necessary
 * TaskManager, defines the agent's capabilities, and sets up an AgentCard with the
 * agent's details.
 *
 * @param wait Whether the server should block the current thread. Default is true.
 * @param port The port on which the server will listen. If null, defaults to 5001.
 * @throws IllegalStateException If no agents are found in the ArcAgents instance.
 */
fun ArcAgents.serveA2A(
    wait: Boolean = true,
    port: Int? = null,
) {
    // Find the agent to call.
    val agent = getAgents().firstOrNull() as ConversationAgent?
        ?: throw IllegalStateException("No agents found. Please add an agent to the ArcAgents instance.")

    // Create a TaskManager with your TaskHandler
    val taskManager = BasicTaskManager(AgentTaskHandler(agent, this))

    // Define your Agent's capabilities
    val capabilities = Capabilities(
        streaming = true,
        pushNotifications = true,
        stateTransitionHistory = false,
    )

    // Create an AgentCard for your Agent
    val agentCard = AgentCard(
        name = agent.name,
        description = agent.description,
        version = "1.0.0",
        capabilities = capabilities,
        defaultInputModes = listOf("text"),
        defaultOutputModes = listOf("text"),
        url = "",
        skills = agent.skills?.map {
            Skill(
                id = it.id,
                name = it.name,
                description = it.description,
            )
        } ?: emptyList(),
    )

    // Create and start the A2A Server
    val server = A2AServer(
        port = port ?: 5001,
        endpoint = "/",
        agentCard = agentCard,
        taskManager = taskManager,
    )

    // Start the server
    server.start(wait = wait)
}
