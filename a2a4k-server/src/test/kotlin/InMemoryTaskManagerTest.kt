// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.a2a4k.InMemoryTaskManager
import org.a2a4k.models.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class InMemoryTaskManagerTest {

    private lateinit var taskManager: InMemoryTaskManager
    
    @BeforeEach
    fun setup() {
        taskManager = InMemoryTaskManager(NoopTaskHandler())
    }
    
    @Test
    fun `test onGetTask returns task not found error when task does not exist`() = runBlocking {
        // Given
        val taskId = "non-existent-task"
        val requestId = "req-123"
        val request = GetTaskRequest(
            id = requestId,
            params = TaskQueryParams(id = taskId, historyLength = 10)
        )
        
        // When
        val response = taskManager.onGetTask(request)
        
        // Then
        assertEquals(requestId, response.id)
        assertNull(response.result)
        assertNotNull(response.error)
        assertEquals(-32001, response.error?.code)
    }
    
    @Test
    fun `test onSendTask creates a new task when it does not exist`() = runBlocking {
        // Given
        val taskId = "new-task-123"
        val sessionId = "session-123"
        val requestId = "req-456"
        val textPart = TextPart(text = "Hello", metadata = emptyMap())
        val message = Message(role = "user", parts = listOf(textPart), metadata = null)
        val request = SendTaskRequest(
            id = requestId,
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = 10
            )
        )
        
        // When
        val response = taskManager.onSendTask(request)
        
        // Then
        assertEquals(requestId, response.id)
        assertNotNull(response.result)
        assertNull(response.error)
        assertEquals(taskId, response.result?.id)
        assertEquals(sessionId, response.result?.sessionId)
        assertEquals(TaskState.submitted, response.result?.status?.state)
        assertEquals(1, response.result?.history?.size)
    }
    
    @Test
    fun `test onSendTask updates an existing task`() = runBlocking {
        // Given
        val taskId = "existing-task-123"
        val sessionId = "session-123"
        val requestId = "req-789"
        
        // Create initial task
        val textPart1 = TextPart(text = "Hello", metadata = emptyMap())
        val message1 = Message(role = "user", parts = listOf(textPart1), metadata = null)
        val request1 = SendTaskRequest(
            id = "req-initial",
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message1,
                historyLength = 10
            )
        )
        taskManager.onSendTask(request1)
        
        // Update task
        val textPart2 = TextPart(text = "How are you?", metadata = emptyMap())
        val message2 = Message(role = "user", parts = listOf(textPart2), metadata = null)
        val request2 = SendTaskRequest(
            id = requestId,
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message2,
                historyLength = 10
            )
        )
        
        // When
        val response = taskManager.onSendTask(request2)
        
        // Then
        assertEquals(requestId, response.id)
        assertNotNull(response.result)
        assertNull(response.error)
        assertEquals(taskId, response.result?.id)
        assertEquals(sessionId, response.result?.sessionId)
        assertEquals(TaskState.submitted, response.result?.status?.state)
        assertEquals(2, response.result?.history?.size)
    }
    
    @Test
    fun `test onCancelTask returns task not found error when task does not exist`() = runBlocking {
        // Given
        val taskId = "non-existent-task"
        val requestId = "req-cancel"
        val request = CancelTaskRequest(
            id = requestId,
            params = TaskIdParams(id = taskId)
        )
        
        // When
        val response = taskManager.onCancelTask(request)
        
        // Then
        assertEquals(requestId, response.id)
        assertNull(response.result)
        assertNotNull(response.error)
        assertEquals(-32001, response.error?.code)
    }
    
    @Test
    fun `test onCancelTask returns task not cancelable error for existing task`() = runBlocking {
        // Given
        val taskId = "existing-task-456"
        val sessionId = "session-456"
        val requestId = "req-cancel-existing"
        
        // Create task
        val textPart = TextPart(text = "Hello", metadata = emptyMap())
        val message = Message(role = "user", parts = listOf(textPart), metadata = null)
        val createRequest = SendTaskRequest(
            id = "req-create",
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = 10
            )
        )
        taskManager.onSendTask(createRequest)
        
        // Try to cancel task
        val cancelRequest = CancelTaskRequest(
            id = requestId,
            params = TaskIdParams(id = taskId)
        )
        
        // When
        val response = taskManager.onCancelTask(cancelRequest)
        
        // Then
        assertEquals(requestId, response.id)
        assertNull(response.result)
        assertNotNull(response.error)
        assertEquals(-32002, response.error?.code)
    }
    
    @Test
    fun `test onSetTaskPushNotification sets push notification for existing task`() = runBlocking {
        // Given
        val taskId = "existing-task-789"
        val sessionId = "session-789"
        val requestId = "req-push"
        
        // Create task
        val textPart = TextPart(text = "Hello", metadata = emptyMap())
        val message = Message(role = "user", parts = listOf(textPart), metadata = null)
        val createRequest = SendTaskRequest(
            id = "req-create",
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = 10
            )
        )
        taskManager.onSendTask(createRequest)
        
        // Set push notification
        val pushConfig = PushNotificationConfig(url = "https://example.com/push")
        val pushRequest = SetTaskPushNotificationRequest(
            id = requestId,
            params = TaskPushNotificationConfig(id = taskId, pushNotificationConfig = pushConfig)
        )
        
        // When
        val response = taskManager.onSetTaskPushNotification(pushRequest)
        
        // Then
        assertEquals(requestId, response.id)
        assertNotNull(response.result)
        assertNull(response.error)
        assertEquals(taskId, response.result?.id)
        assertEquals(pushConfig.url, response.result?.pushNotificationConfig?.url)
    }
    
    @Test
    fun `test onGetTaskPushNotification returns push notification for existing task`() = runBlocking {
        // Given
        val taskId = "existing-task-push"
        val sessionId = "session-push"
        val requestId = "req-get-push"
        
        // Create task
        val textPart = TextPart(text = "Hello", metadata = emptyMap())
        val message = Message(role = "user", parts = listOf(textPart), metadata = null)
        val createRequest = SendTaskRequest(
            id = "req-create",
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = 10
            )
        )
        taskManager.onSendTask(createRequest)
        
        // Set push notification
        val pushConfig = PushNotificationConfig(url = "https://example.com/push")
        val pushRequest = SetTaskPushNotificationRequest(
            id = "req-set-push",
            params = TaskPushNotificationConfig(id = taskId, pushNotificationConfig = pushConfig)
        )
        taskManager.onSetTaskPushNotification(pushRequest)
        
        // Get push notification
        val getRequest = GetTaskPushNotificationRequest(
            id = requestId,
            params = TaskIdParams(id = taskId)
        )
        
        // When
        val response = taskManager.onGetTaskPushNotification(getRequest)
        
        // Then
        assertEquals(requestId, response.id)
        assertNotNull(response.result)
        assertNull(response.error)
        assertEquals(taskId, response.result?.id)
        assertEquals(pushConfig.url, response.result?.pushNotificationConfig?.url)
    }
    
    @Test
    fun `test onSendTaskSubscribe creates a new task and returns a flow`() = runBlocking {
        // Given
        val taskId = "streaming-task-123"
        val sessionId = "session-stream"
        val requestId = "req-stream"
        val textPart = TextPart(text = "Hello streaming", metadata = emptyMap())
        val message = Message(role = "user", parts = listOf(textPart), metadata = null)
        val request = SendTaskStreamingRequest(
            id = requestId,
            params = TaskSendParams(
                id = taskId,
                sessionId = sessionId,
                message = message,
                historyLength = 10
            )
        )
        
        // When
        val responseFlow = taskManager.onSendTaskSubscribe(request)
        val firstResponse = responseFlow.first()
        
        // Then
        assertEquals(requestId, firstResponse.id)
        assertNotNull(firstResponse.result)
        assertNull(firstResponse.error)
        assertEquals(taskId, (firstResponse.result as TaskStatusUpdateEvent).id)
        assertEquals(TaskState.submitted, (firstResponse.result as TaskStatusUpdateEvent).status.state)
    }
    
    @Test
    fun `test onResubscribeToTask returns error for non-existent task`() = runBlocking {
        // Given
        val taskId = "non-existent-task"
        val requestId = "req-resub"
        val request = TaskResubscriptionRequest(
            id = requestId,
            params = TaskQueryParams(id = taskId, historyLength = 10)
        )
        
        // When
        val responseFlow = taskManager.onResubscribeToTask(request)
        val firstResponse = responseFlow.first()
        
        // Then
        assertEquals(requestId, firstResponse.id)
        assertNull(firstResponse.result)
        assertNotNull(firstResponse.error)
        assertEquals(-32001, firstResponse.error?.code)
    }
}