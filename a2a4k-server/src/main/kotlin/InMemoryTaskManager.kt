// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.a2a4k.models.*
import org.a2a4k.models.GetTaskResponse
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * In-memory implementation of the TaskManager interface.
 *
 * This class provides an implementation of the TaskManager interface that stores
 * tasks and their associated data in memory. It supports all operations defined
 * in the TaskManager interface, including task creation, retrieval, cancellation,
 * and subscription to task updates.
 */
class InMemoryTaskManager(private val taskHandler: TaskHandler) : TaskManager {

    /** Logger for this class */
    private val log = LoggerFactory.getLogger(this::class.java)

    /** Map of task IDs to Task objects */
    private val tasks: MutableMap<String, Task> = ConcurrentHashMap()

    /** Map of task IDs to their push notification configurations */
    private val pushNotificationInfos: MutableMap<String, PushNotificationConfig> = ConcurrentHashMap()

    /** Mutex for synchronizing access to the tasks and push notification maps */
    private val lock = Mutex()

    /** Map of task IDs to lists of subscribers for server-sent events */
    private val taskSseSubscribers: MutableMap<String, MutableList<Channel<Any>>> = ConcurrentHashMap()

    /** Mutex for synchronizing access to the subscribers map */
    private val subscriberLock = Mutex()

    /**
     * {@inheritDoc}
     *
     * This implementation retrieves a task from the in-memory store by its ID.
     * If the task is not found, it returns an error response.
     */
    override suspend fun onGetTask(request: GetTaskRequest): GetTaskResponse {
        log.info("Getting task ${request.params.id}")
        val taskQueryParams: TaskQueryParams = request.params

        lock.withLock {
            val task = tasks[taskQueryParams.id] ?: return GetTaskResponse(id = request.id, error = TaskNotFoundError())
            val taskResult = appendTaskHistory(task, taskQueryParams.historyLength)
            return GetTaskResponse(id = request.id, result = taskResult)
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation attempts to cancel a task in the in-memory store.
     * Currently, tasks are not cancelable, so it always returns a TaskNotCancelableError.
     * If the task is not found, it returns a TaskNotFoundError.
     */
    override suspend fun onCancelTask(request: CancelTaskRequest): CancelTaskResponse {
        log.info("Cancelling task ${request.params.id}")
        val taskIdParams: TaskIdParams = request.params

        lock.withLock {
            val task = tasks[taskIdParams.id]
            if (task == null) {
                return CancelTaskResponse(id = request.id, error = TaskNotFoundError())
            }

            return CancelTaskResponse(id = request.id, error = TaskNotCancelableError())
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation creates or updates a task in the in-memory store.
     * If a push notification configuration is provided, it is stored as well.
     * If an error occurs during the operation, an InternalError is returned.
     */
    override suspend fun onSendTask(request: SendTaskRequest): SendTaskResponse {
        log.info("Sending task ${request.params.id}")
        val taskSendParams: TaskSendParams = request.params

        return try {
            // Create or update the task
            val task = upsertTask(taskSendParams)

            // Set push notification if provided
            taskSendParams.pushNotification?.let {
                setPushNotificationInfo(taskSendParams.id, it)
            }

            val handledTask = taskHandler.handle(task)
            tasks[taskSendParams.id] = handledTask

            // Return the task with appropriate history length
            val taskResult = appendTaskHistory(handledTask, taskSendParams.historyLength)
            SendTaskResponse(id = request.id, result = taskResult)
        } catch (e: Exception) {
            log.error("Error while sending task: ${e.message}")
            SendTaskResponse(
                id = request.id,
                error = InternalError()
            )
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation creates or updates a task in the in-memory store and sets up
     * a subscription for streaming updates. It returns a flow of streaming responses
     * with task updates.
     */
    override suspend fun onSendTaskSubscribe(request: SendTaskStreamingRequest): Flow<SendTaskStreamingResponse> {
        log.info("Sending task with subscription ${request.params.id}")
        val taskSendParams: TaskSendParams = request.params

        try {
            // Create or update the task
            val task = upsertTask(taskSendParams)

            // Set push notification if provided
            taskSendParams.pushNotification?.let {
                setPushNotificationInfo(taskSendParams.id, it)
            }

            // Set up SSE consumer
            val sseEventQueue = setupSseConsumer(taskSendParams.id)

            // Send initial task status update
            val initialStatusEvent = TaskStatusUpdateEvent(
                id = task.id,
                status = task.status,
                final = false
            )
            enqueueEventsForSse(task.id, initialStatusEvent)

            // Return the flow of events
            return dequeueEventsForSse(request.id ?: "", task.id, sseEventQueue)
        } catch (e: Exception) {
            log.error("Error while setting up task subscription: ${e.message}")
            return flow {
                emit(
                    SendTaskStreamingResponse(
                        id = request.id,
                        error = InternalError()
                    )
                )
            }
        }
    }

    /**
     * Sets the push notification configuration for a task.
     *
     * @param taskId The ID of the task
     * @param notificationConfig The push notification configuration to set
     * @throws IllegalArgumentException if the task is not found
     */
    private suspend fun setPushNotificationInfo(taskId: String, notificationConfig: PushNotificationConfig) {
        lock.withLock {
            tasks[taskId] ?: throw IllegalArgumentException("Task not found for $taskId")
            pushNotificationInfos[taskId] = notificationConfig
        }
    }

    /**
     * Retrieves the push notification configuration for a task.
     *
     * @param taskId The ID of the task
     * @return The push notification configuration, or null if not set
     * @throws IllegalArgumentException if the task is not found
     */
    private suspend fun getPushNotificationInfo(taskId: String): PushNotificationConfig? {
        lock.withLock {
            tasks[taskId] ?: throw IllegalArgumentException("Task not found for $taskId")
            return pushNotificationInfos[taskId]
        }
    }

    /**
     * Checks if a push notification configuration exists for a task.
     *
     * @param taskId The ID of the task
     * @return true if a push notification configuration exists, false otherwise
     */
    private fun hasPushNotificationInfo(taskId: String): Boolean {
        return pushNotificationInfos.containsKey(taskId)
    }

    /**
     * {@inheritDoc}
     *
     * This implementation sets the push notification configuration for a task in the in-memory store.
     * If the task is not found or an error occurs, it returns an error response.
     */
    override suspend fun onSetTaskPushNotification(request: SetTaskPushNotificationRequest): SetTaskPushNotificationResponse {
        log.info("Setting task push notification ${request.params.id}")
        val taskNotificationParams: TaskPushNotificationConfig = request.params

        return try {
            setPushNotificationInfo(taskNotificationParams.id, taskNotificationParams.pushNotificationConfig)
            SetTaskPushNotificationResponse(id = request.id, result = taskNotificationParams)
        } catch (e: Exception) {
            log.error("Error while setting push notification info: ${e.message}")
            SetTaskPushNotificationResponse(
                id = request.id,
                error = InternalError()
            )
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation retrieves the push notification configuration for a task from the in-memory store.
     * If the task is not found, the configuration is not set, or an error occurs, it returns an error response.
     */
    override suspend fun onGetTaskPushNotification(request: GetTaskPushNotificationRequest): GetTaskPushNotificationResponse {
        log.info("Getting task push notification ${request.params.id}")
        val taskParams: TaskIdParams = request.params

        return try {
            val notificationInfo = getPushNotificationInfo(taskParams.id)
            if (notificationInfo != null) {
                GetTaskPushNotificationResponse(
                    id = request.id,
                    result = TaskPushNotificationConfig(id = taskParams.id, pushNotificationConfig = notificationInfo)
                )
            } else {
                GetTaskPushNotificationResponse(
                    id = request.id,
                    error = InternalError()
                )
            }
        } catch (e: Exception) {
            log.error("Error while getting push notification info: ${e.message}")
            GetTaskPushNotificationResponse(
                id = request.id,
                error = InternalError()
            )
        }
    }

    /**
     * Creates a new task or updates an existing one.
     *
     * @param taskSendParams The parameters for creating or updating the task
     * @return The created or updated task
     */
    private suspend fun upsertTask(taskSendParams: TaskSendParams): Task {
        log.info("Upserting task ${taskSendParams.id}")
        return lock.withLock {
            val task = tasks[taskSendParams.id]
            if (task == null) {
                val newTask = Task(
                    id = taskSendParams.id,
                    sessionId = UUID.randomUUID().toString(),
                    status = TaskStatus(state = TaskState.submitted),
                    history = listOf(taskSendParams.message)
                )
                tasks[taskSendParams.id] = newTask
                newTask
            } else {
                // Create a new task with updated history
                val updatedHistory = task.history?.toMutableList() ?: mutableListOf()
                updatedHistory.add(taskSendParams.message)
                val updatedTask = task.copy(history = updatedHistory)
                tasks[taskSendParams.id] = updatedTask
                updatedTask
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * This implementation resubscribes to a task in the in-memory store and sets up
     * a subscription for streaming updates. It returns a flow of streaming responses
     * with task updates.
     */
    override suspend fun onResubscribeToTask(request: TaskResubscriptionRequest): Flow<SendTaskStreamingResponse> {
        log.info("Resubscribing to task ${request.params.id}")
        val taskQueryParams: TaskQueryParams = request.params

        return try {
            lock.withLock {
                val task = tasks[taskQueryParams.id]
                if (task == null) {
                    return flow {
                        emit(
                            SendTaskStreamingResponse(
                                id = request.id,
                                error = TaskNotFoundError()
                            )
                        )
                    }
                }

                // Set up SSE consumer with resubscribe flag
                val sseEventQueue = setupSseConsumer(taskQueryParams.id, isResubscribe = true)

                // Send current task status update
                val statusEvent = TaskStatusUpdateEvent(
                    id = task.id,
                    status = task.status,
                    final = false
                )
                enqueueEventsForSse(task.id, statusEvent)

                // Return the flow of events
                dequeueEventsForSse(request.id ?: "", task.id, sseEventQueue)
            }
        } catch (e: Exception) {
            log.error("Error while resubscribing to task: ${e.message}")
            flow {
                emit(
                    SendTaskStreamingResponse(
                        id = request.id,
                        error = InternalError()
                    )
                )
            }
        }
    }

    /**
     * Updates a task's status and artifacts in the store.
     *
     * @param taskId The ID of the task to update
     * @param status The new status for the task
     * @param artifacts The new artifacts for the task, or null to keep existing artifacts
     * @return The updated task
     * @throws IllegalArgumentException if the task is not found
     */
    private suspend fun updateStore(taskId: String, status: TaskStatus, artifacts: List<Artifact>?): Task {
        return lock.withLock {
            val task = tasks[taskId] ?: throw IllegalArgumentException("Task $taskId not found")

            // Create updated history
            val updatedHistory = task.history?.toMutableList() ?: mutableListOf()
            status.message?.let { updatedHistory.add(it) }

            // Create updated artifacts
            val updatedArtifacts = if (artifacts != null) {
                val currentArtifacts = task.artifacts?.toMutableList() ?: mutableListOf()
                currentArtifacts.addAll(artifacts)
                currentArtifacts
            } else {
                task.artifacts
            }

            // Create a new task with updated fields
            val updatedTask = task.copy(
                status = status,
                history = updatedHistory,
                artifacts = updatedArtifacts
            )

            // Update the task in the map
            tasks[taskId] = updatedTask

            updatedTask
        }
    }

    /**
     * Creates a copy of the task with a limited history length.
     *
     * @param task The task to process
     * @param historyLength The maximum number of history items to include, or null for no history
     * @return A copy of the task with limited history
     */
    private fun appendTaskHistory(task: Task, historyLength: Int?): Task {
        val updatedHistory = if (historyLength != null && historyLength > 0) {
            task.history?.let { history ->
                history.takeLast(historyLength).toMutableList()
            } ?: emptyList()
        } else {
            emptyList()
        }
        return task.copy(history = updatedHistory)
    }

    /**
     * Sets up a subscriber for server-sent events for a task.
     *
     * @param taskId The ID of the task to subscribe to
     * @param isResubscribe Whether this is a resubscription to an existing task
     * @return A channel for receiving events
     * @throws IllegalArgumentException if resubscribing to a non-existent task
     */
    private suspend fun setupSseConsumer(taskId: String, isResubscribe: Boolean = false): Channel<Any> {
        return subscriberLock.withLock {
            if (!taskSseSubscribers.containsKey(taskId)) {
                if (isResubscribe) {
                    throw IllegalArgumentException("Task not found for resubscription")
                } else {
                    taskSseSubscribers[taskId] = mutableListOf()
                }
            }

            val sseEventQueue = Channel<Any>(Channel.UNLIMITED)
            taskSseSubscribers[taskId]?.add(sseEventQueue)
            sseEventQueue
        }
    }

    /**
     * Sends an event to all subscribers of a task.
     *
     * @param taskId The ID of the task
     * @param taskUpdateEvent The event to send
     */
    private suspend fun enqueueEventsForSse(taskId: String, taskUpdateEvent: Any) {
        subscriberLock.withLock {
            taskSseSubscribers[taskId]?.forEach { subscriber ->
                subscriber.send(taskUpdateEvent)
            }
        }
    }

    /**
     * Creates a flow of events from a subscriber channel.
     *
     * @param requestId The ID of the request
     * @param taskId The ID of the task
     * @param sseEventQueue The channel to receive events from
     * @return A flow of streaming responses
     */
    private fun dequeueEventsForSse(
        requestId: String,
        taskId: String,
        sseEventQueue: Channel<Any>
    ): Flow<SendTaskStreamingResponse> = flow {
        try {
            for (event in sseEventQueue) {
                if (event is JsonRpcError) {
                    emit(SendTaskStreamingResponse(id = requestId, error = event))
                    break
                }
                if (event is TaskStreamingResult) {
                    emit(SendTaskStreamingResponse(id = requestId, result = event))
                    if (event is TaskStatusUpdateEvent && event.final) {
                        break
                    }
                }
            }
        } finally {
            runBlocking {
                subscriberLock.withLock {
                    taskSseSubscribers[taskId]?.remove(sseEventQueue)
                }
            }
        }
    }
}
