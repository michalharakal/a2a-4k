// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import org.a2a4k.models.*
import org.a2a4k.models.GetTaskResponse

/**
 * Interface for managing tasks in the A2A system.
 *
 * This interface defines methods for creating, retrieving, updating, and subscribing to tasks.
 * It also provides functionality for managing push notifications related to tasks.
 */
interface TaskManager {
    /**
     * Retrieves a task by its ID.
     *
     * @param request The request containing the task ID and query parameters
     * @return A response containing the requested task or an error if the task is not found
     */
    suspend fun onGetTask(request: GetTaskRequest): GetTaskResponse

    /**
     * Attempts to cancel a task.
     *
     * @param request The request containing the task ID to cancel
     * @return A response indicating success or failure of the cancellation
     */
    suspend fun onCancelTask(request: CancelTaskRequest): CancelTaskResponse

    /**
     * Creates or updates a task.
     *
     * @param request The request containing the task data to send
     * @return A response containing the created or updated task
     */
    suspend fun onSendTask(request: SendTaskRequest): SendTaskResponse

    /**
     * Subscribes to streaming updates for a task.
     *
     * @param request The request containing the task data and subscription parameters
     * @return A flow of streaming responses with task updates
     */
    suspend fun onSendTaskSubscribe(request: SendTaskStreamingRequest): Flow<SendTaskStreamingResponse>

    /**
     * Sets push notification configuration for a task.
     *
     * @param request The request containing the task ID and push notification configuration
     * @return A response indicating success or failure of the operation
     */
    suspend fun onSetTaskPushNotification(request: SetTaskPushNotificationRequest): SetTaskPushNotificationResponse

    /**
     * Retrieves the push notification configuration for a task.
     *
     * @param request The request containing the task ID
     * @return A response containing the push notification configuration or an error if not found
     */
    suspend fun onGetTaskPushNotification(request: GetTaskPushNotificationRequest): GetTaskPushNotificationResponse

    /**
     * Resubscribes to a task to receive streaming updates.
     *
     * @param request The request containing the task ID and resubscription parameters
     * @return A flow of streaming responses with task updates
     */
    suspend fun onResubscribeToTask(request: TaskResubscriptionRequest): Flow<SendTaskStreamingResponse>
}
