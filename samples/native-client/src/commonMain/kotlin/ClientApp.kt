// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

import io.github.a2a_4k.A2AClient
import io.github.a2a_4k.models.toUserMessage
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Shared client application logic for connecting to the A2A JVM server.
 * This demonstrates cross-platform native client communication.
 */
class ClientApp {
    suspend fun run(serverUrl: String = "http://localhost:5000") {
        println("Starting A2A Native Client...")
        println("Connecting to server: $serverUrl")
        
        val client = A2AClient(baseUrl = serverUrl)
        
        try {
            // Get agent information
            val agentCard = client.getAgentCard()
            println("Connected to: ${agentCard.name}")
            println("Description: ${agentCard.description}")
            println("Version: ${agentCard.version}")
            println("Capabilities: streaming=${agentCard.capabilities.streaming}, auth=${agentCard.authentication}")
            
            // Send a few test messages
            val messages = listOf(
                "Hello from native client!",
                "Testing A2A communication",
                "This message was sent from a native executable",
                "Cross-platform communication working!"
            )
            
            messages.forEachIndexed { index, messageText ->
                println("\n--- Message ${index + 1}/${messages.size} ---")
                println("Sending: $messageText")
                
                val taskId = "task-${Random.nextInt(1000, 9999)}"
                val sessionId = "native-session-${Random.nextInt(100, 999)}"
                
                try {
                    val response = client.sendTask(
                        message = messageText.toUserMessage(),
                        taskId = taskId,
                        sessionId = sessionId
                    )
                    
                    when {
                        response.result != null -> {
                            val task = response.result
                            val lastMessage = task?.history?.lastOrNull()
                            val content = lastMessage?.parts?.firstOrNull()?.let { part ->
                                if (part is io.github.a2a_4k.models.TextPart) part.text else "Non-text response"
                            } ?: "No response content"
                            
                            println("✓ Server response: $content")
                            println("  Task ID: ${task?.id}")
                            println("  Status: ${task?.status?.state}")
                        }
                        response.error != null -> {
                            println("✗ Error: ${response.error?.message}")
                            println("  Code: ${response.error?.code}")
                        }
                    }
                } catch (e: Exception) {
                    println("✗ Request failed: ${e.message}")
                }
                
                // Small delay between messages to avoid overwhelming the server
                if (index < messages.size - 1) {
                    delay(1000)
                }
            }
            
            println("\n=== Communication Complete ===")
            
        } catch (e: Exception) {
            println("Client initialization error: ${e.message}")
            e.printStackTrace()
        } finally {
            client.close()
            println("Client disconnected.")
        }
    }
}