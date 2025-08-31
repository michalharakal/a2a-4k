// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

import io.github.a2a_4k.*
import io.github.a2a_4k.models.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking

/**
 * Example TaskHandler that echoes back messages with a JVM server prefix.
 * Demonstrates the proper Flow<TaskUpdate> API usage.
 */
class EchoTaskHandler : TaskHandler {
    override fun handle(task: Task): Flow<TaskUpdate> = flow {
        val lastMessage = task.history?.lastOrNull() ?: error("No message found")
        val content = (lastMessage.parts.firstOrNull() as? TextPart)?.text ?: "Unknown"
        
        // Simulate some processing time
        delay(500)
        
        val response = "Echo from JVM Server: $content"
        
        // Emit artifact update
        emit(ArtifactUpdate(listOf(
            Artifact(
                name = "response",
                parts = listOf(TextPart(text = response))
            )
        )))
        
        // Emit completion status
        emit(StatusUpdate(
            TaskStatus(
                TaskState.COMPLETED, 
                message = assistantMessage(response)
            )
        ))
    }
}

/**
 * Main function that starts the JVM A2A server.
 */
fun main() = runBlocking {
    val taskHandler = EchoTaskHandler()
    val taskManager = BasicTaskManager(taskHandler)
    
    val agentCard = AgentCard(
        name = "JVM Echo Agent",
        description = "A simple echo agent running on JVM that repeats messages with a server prefix",
        url = "http://localhost:5000",
        version = "1.0.0",
        capabilities = Capabilities(
            streaming = true,
        ),
        defaultInputModes = listOf("text"),
        defaultOutputModes = listOf("text"),
        skills = listOf()
    )
    
    val server = A2AServer(
        host = "0.0.0.0",
        port = 5000,
        endpoint = "/",
        agentCard = agentCard,
        taskManager = taskManager
    )
    
    println("Starting JVM A2A Server on port 5000...")
    println("Agent: ${agentCard.name}")
    println("Description: ${agentCard.description}")
    println("Press Ctrl+C to stop")
    
    server.start(wait = true) // Block and wait
}