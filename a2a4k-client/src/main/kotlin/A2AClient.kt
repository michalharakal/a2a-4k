// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.a2a4k.models.*

/**
 * A2AClient implements an Agent-to-Agent communication client based on the A2A protocol.
 *
 * This client provides methods for interacting with an A2A server, including retrieving
 * the agent's metadata (agent card), sending and retrieving tasks, canceling tasks,
 * and managing push notification configurations.
 */
class A2AClient(
    /**
     * The base URL of the A2A server.
     * Example: "http://localhost:5000"
     */
    private val baseUrl: String,
    
    /**
     * The endpoint path for the server's API.
     * Default is "/".
     */
    private val endpoint: String = "/",
    
    /**
     * Custom HTTP client to use for requests.
     * If not provided, a default client will be created.
     */
    private val httpClient: HttpClient? = null
) {
    /**
     * The JSON serializer/deserializer configured to ignore unknown keys in the input.
     */
    private val json = Json { ignoreUnknownKeys = true }
    
    /**
     * The HTTP client used for making requests to the server.
     */
    private val client: HttpClient = httpClient ?: HttpClient(CIO) {
        install(ContentNegotiation) {
            json(json)
        }
        install(SSE)
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 60000
        }
    }
    
    /**
     * The full URL for the API endpoint.
     */
    private val apiUrl = "$baseUrl$endpoint"
    
    /**
     * The URL for retrieving the agent card.
     */
    private val agentCardUrl = "$baseUrl/.well-known/agent.json"
    
    /**
     * Retrieves the agent card from the server.
     *
     * @return The agent card containing metadata about the agent.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun getAgentCard(): AgentCard {
        val response = client.get(agentCardUrl)
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to get agent card: ${response.status}")
        }
        
        val responseBody = response.bodyAsText()
        return json.decodeFromString(responseBody)
    }
    
    /**
     * Retrieves a task by its ID.
     *
     * @param taskId The ID of the task to retrieve.
     * @param historyLength The maximum number of history entries to include in the response.
     * @param requestId A unique identifier for this request.
     * @return The response containing the requested task or an error if the task is not found.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun getTask(taskId: String, historyLength: Int = 10, requestId: String = generateRequestId()): GetTaskResponse {
        val request = GetTaskRequest(
            id = requestId,
            params = TaskQueryParams(id = taskId, historyLength = historyLength)
        )
        
        val response = client.post(apiUrl) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GetTaskRequest.serializer(), request))
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to get task: ${response.status}")
        }
        
        val responseBody = response.bodyAsText()
        return json.decodeFromString(responseBody)
    }
    
    /**
     * Creates or updates a task.
     *
     * @param taskId The ID of the task to create or update.
     * @param sessionId The session ID for this task.
     * @param message The message to include in the task.
     * @param historyLength The maximum number of history entries to include in the response.
     * @param requestId A unique identifier for this request.
     * @return The response containing the created or updated task.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun sendTask(
        taskId: String,
        sessionId: String,
        message: Message,
        historyLength: Int = 10,
        requestId: String = generateRequestId()
    ): SendTaskResponse {
        val request = SendTaskRequest(
            id = requestId,
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = historyLength
            )
        )
        
        val response = client.post(apiUrl) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SendTaskRequest.serializer(), request))
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to send task: ${response.status}")
        }
        
        val responseBody = response.bodyAsText()
        return json.decodeFromString(responseBody)
    }
    
    /**
     * Subscribes to streaming updates for a task.
     *
     * @param taskId The ID of the task to subscribe to.
     * @param sessionId The session ID for this task.
     * @param message The message to include in the task.
     * @param historyLength The maximum number of history entries to include in the response.
     * @param requestId A unique identifier for this request.
     * @return A flow of streaming responses with task updates.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun sendTaskStreaming(
        taskId: String,
        sessionId: String,
        message: Message,
        historyLength: Int = 10,
        requestId: String = generateRequestId()
    ): Flow<SendTaskStreamingResponse> {
        val request = SendTaskStreamingRequest(
            id = requestId,
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = historyLength
            )
        )
        
        return flow {
            client.sse(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(SendTaskStreamingRequest.serializer(), request))
            }.collect { serverSentEvent ->
                val responseBody = serverSentEvent.data
                val response = json.decodeFromString<SendTaskStreamingResponse>(responseBody)
                emit(response)
            }
        }
    }
    
    /**
     * Attempts to cancel a task.
     *
     * @param taskId The ID of the task to cancel.
     * @param requestId A unique identifier for this request.
     * @return The response indicating success or failure of the cancellation.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun cancelTask(taskId: String, requestId: String = generateRequestId()): CancelTaskResponse {
        val request = CancelTaskRequest(
            id = requestId,
            params = TaskIdParams(id = taskId)
        )
        
        val response = client.post(apiUrl) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(CancelTaskRequest.serializer(), request))
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to cancel task: ${response.status}")
        }
        
        val responseBody = response.bodyAsText()
        return json.decodeFromString(responseBody)
    }
    
    /**
     * Sets push notification configuration for a task.
     *
     * @param taskId The ID of the task to configure.
     * @param config The push notification configuration.
     * @param requestId A unique identifier for this request.
     * @return The response indicating success or failure of the operation.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun setTaskPushNotification(
        taskId: String,
        config: PushNotificationConfig,
        requestId: String = generateRequestId()
    ): SetTaskPushNotificationResponse {
        val request = SetTaskPushNotificationRequest(
            id = requestId,
            params = TaskPushNotificationConfig(
                id = taskId,
                config = config
            )
        )
        
        val response = client.post(apiUrl) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(SetTaskPushNotificationRequest.serializer(), request))
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to set task push notification: ${response.status}")
        }
        
        val responseBody = response.bodyAsText()
        return json.decodeFromString(responseBody)
    }
    
    /**
     * Retrieves the push notification configuration for a task.
     *
     * @param taskId The ID of the task.
     * @param requestId A unique identifier for this request.
     * @return The response containing the push notification configuration or an error if not found.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun getTaskPushNotification(
        taskId: String,
        requestId: String = generateRequestId()
    ): GetTaskPushNotificationResponse {
        val request = GetTaskPushNotificationRequest(
            id = requestId,
            params = TaskIdParams(id = taskId)
        )
        
        val response = client.post(apiUrl) {
            contentType(ContentType.Application.Json)
            setBody(json.encodeToString(GetTaskPushNotificationRequest.serializer(), request))
        }
        
        if (response.status != HttpStatusCode.OK) {
            throw Exception("Failed to get task push notification: ${response.status}")
        }
        
        val responseBody = response.bodyAsText()
        return json.decodeFromString(responseBody)
    }
    
    /**
     * Resubscribes to a task to receive streaming updates.
     *
     * @param taskId The ID of the task to resubscribe to.
     * @param requestId A unique identifier for this request.
     * @return A flow of streaming responses with task updates.
     * @throws Exception if the request fails or the response cannot be parsed.
     */
    suspend fun resubscribeToTask(
        taskId: String,
        requestId: String = generateRequestId()
    ): Flow<SendTaskStreamingResponse> {
        val request = TaskResubscriptionRequest(
            id = requestId,
            params = TaskIdParams(id = taskId)
        )
        
        return flow {
            client.sse(apiUrl) {
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(TaskResubscriptionRequest.serializer(), request))
            }.collect { serverSentEvent ->
                val responseBody = serverSentEvent.data
                val response = json.decodeFromString<SendTaskStreamingResponse>(responseBody)
                emit(response)
            }
        }
    }
    
    /**
     * Closes the HTTP client and releases resources.
     */
    fun close() {
        client.close()
    }
    
    /**
     * Generates a unique request ID.
     *
     * @return A unique string ID.
     */
    private fun generateRequestId(): String {
        return "req-${System.currentTimeMillis()}-${(0..999).random()}"
    }
}