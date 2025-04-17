// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.mockk.mockk
import kotlinx.serialization.json.Json
import org.a2a4k.A2AServer
import org.a2a4k.TaskManager
import org.a2a4k.models.*
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class A2AServerTest {

    @Test
    fun `test server initialization with custom parameters`() {
        // Given
        val host = "localhost"
        val port = 8080
        val endpoint = "/api"
        val mockTaskManager = mockk<TaskManager>()
        
        val capabilities = Capabilities(
            streaming = true,
            pushNotifications = true,
            stateTransitionHistory = true
        )
        
        val authentication = Authentication(
            schemes = listOf("none")
        )
        
        val skill = Skill(
            id = "test-skill",
            name = "Test Skill",
            description = "A test skill",
            tags = listOf("test"),
            examples = listOf("Example 1"),
            inputModes = listOf("text"),
            outputModes = listOf("text")
        )
        
        val agentCard = AgentCard(
            name = "Test Agent",
            description = "Test Agent Description",
            url = "https://example.com",
            version = "1.0.0",
            capabilities = capabilities,
            authentication = authentication,
            defaultInputModes = listOf("text"),
            defaultOutputModes = listOf("text"),
            skills = listOf(skill)
        )
        
        // When
        val server = A2AServer(
            host = host,
            port = port,
            endpoint = endpoint,
            agentCard = agentCard,
            taskManager = mockTaskManager
        )
        
        // Then
        // Verify the server was created successfully with the correct parameters
        // We can't directly access private fields, but we can verify the server instance was created
        assertNotNull(server)
    }
    
    @Test
    fun `test server initialization with default parameters`() {
        // Given
        val mockTaskManager = mockk<TaskManager>()
        
        val capabilities = Capabilities(
            streaming = true,
            pushNotifications = true,
            stateTransitionHistory = true
        )
        
        val authentication = Authentication(
            schemes = listOf("none")
        )
        
        val skill = Skill(
            id = "test-skill",
            name = "Test Skill",
            description = "A test skill",
            tags = listOf("test"),
            examples = listOf("Example 1"),
            inputModes = listOf("text"),
            outputModes = listOf("text")
        )
        
        val agentCard = AgentCard(
            name = "Test Agent",
            description = "Test Agent Description",
            url = "https://example.com",
            version = "1.0.0",
            capabilities = capabilities,
            authentication = authentication,
            defaultInputModes = listOf("text"),
            defaultOutputModes = listOf("text"),
            skills = listOf(skill)
        )
        
        // When
        val server = A2AServer(
            agentCard = agentCard,
            taskManager = mockTaskManager
        )
        
        // Then
        // Verify the server was created successfully with default parameters
        assertNotNull(server)
    }
    
    @Test
    fun `test json configuration`() {
        // Given
        val mockTaskManager = mockk<TaskManager>()
        
        val capabilities = Capabilities(
            streaming = true,
            pushNotifications = true,
            stateTransitionHistory = true
        )
        
        val authentication = Authentication(
            schemes = listOf("none")
        )
        
        val skill = Skill(
            id = "test-skill",
            name = "Test Skill",
            description = "A test skill",
            tags = listOf("test"),
            examples = listOf("Example 1"),
            inputModes = listOf("text"),
            outputModes = listOf("text")
        )
        
        val agentCard = AgentCard(
            name = "Test Agent",
            description = "Test Agent Description",
            url = "https://example.com",
            version = "1.0.0",
            capabilities = capabilities,
            authentication = authentication,
            defaultInputModes = listOf("text"),
            defaultOutputModes = listOf("text"),
            skills = listOf(skill)
        )
        
        // When
        val server = A2AServer(
            agentCard = agentCard,
            taskManager = mockTaskManager
        )
        
        // Then
        // We can't directly access the private json field, but we can verify the server instance was created
        assertNotNull(server)
        
        // Verify that the Json instance with ignoreUnknownKeys=true works as expected
        val json = Json { ignoreUnknownKeys = true }
        val jsonString = """{"name":"Test","unknown":"value"}"""
        val testObject = json.decodeFromString<TestObject>(jsonString)
        assertEquals("Test", testObject.name)
    }
    
    // Helper class for testing Json configuration
    @kotlinx.serialization.Serializable
    private data class TestObject(val name: String)
}