// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.util.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.a2a4k.models.*
import org.a2a4k.models.GetTaskResponse
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap


interface TaskManager {
    suspend fun onGetTask(request: GetTaskRequest): GetTaskResponse
    suspend fun onCancelTask(request: CancelTaskRequest): CancelTaskResponse
    suspend fun onSendTask(request: SendTaskRequest): SendTaskResponse
    suspend fun onSendTaskSubscribe(request: SendTaskStreamingRequest): Flow<SendTaskStreamingResponse>
    suspend fun onSetTaskPushNotification(request: SetTaskPushNotificationRequest): SetTaskPushNotificationResponse
    suspend fun onGetTaskPushNotification(request: GetTaskPushNotificationRequest): GetTaskPushNotificationResponse
    suspend fun onResubscribeToTask(request: TaskResubscriptionRequest): Flow<SendTaskStreamingResponse>
}

class InMemoryTaskManager : TaskManager {

    private val log = LoggerFactory.getLogger(this::class.java)
    private val tasks: MutableMap<String, Task> = ConcurrentHashMap()
    private val pushNotificationInfos: MutableMap<String, PushNotificationConfig> = ConcurrentHashMap()
    private val lock = Mutex()
    private val taskSseSubscribers: MutableMap<String, MutableList<Channel<Any>>> = ConcurrentHashMap()
    private val subscriberLock = Mutex()

    override suspend fun onGetTask(request: GetTaskRequest): GetTaskResponse {
        log.info("Getting task ${request.params.id}")
        val taskQueryParams: TaskQueryParams = request.params

        lock.withLock {
            val task = tasks[taskQueryParams.id] ?: return GetTaskResponse(id = request.id, error = TaskNotFoundError())
            val taskResult = appendTaskHistory(task, taskQueryParams.historyLength)
            return GetTaskResponse(id = request.id, result = taskResult)
        }
    }

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

            // Return the task with appropriate history length
            val taskResult = appendTaskHistory(task, taskSendParams.historyLength)
            SendTaskResponse(id = request.id, result = taskResult)
        } catch (e: Exception) {
            log.error("Error while sending task: ${e.message}")
            SendTaskResponse(
                id = request.id,
                error = InternalError()
            )
        }
    }

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
                emit(SendTaskStreamingResponse(
                    id = request.id,
                    error = InternalError()
                ))
            }
        }
    }

    private suspend fun setPushNotificationInfo(taskId: String, notificationConfig: PushNotificationConfig) {
        lock.withLock {
            val task = tasks[taskId]
            if (task == null) {
                throw IllegalArgumentException("Task not found for $taskId")
            }
            pushNotificationInfos[taskId] = notificationConfig
        }
    }

    private suspend fun getPushNotificationInfo(taskId: String): PushNotificationConfig? {
        lock.withLock {
            val task = tasks[taskId]
            if (task == null) {
                throw IllegalArgumentException("Task not found for $taskId")
            }
            return pushNotificationInfos[taskId]
        }
    }

    private suspend fun hasPushNotificationInfo(taskId: String): Boolean {
        lock.withLock {
            return pushNotificationInfos.containsKey(taskId)
        }
    }

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

    private suspend fun upsertTask(taskSendParams: TaskSendParams): Task {
        log.info("Upserting task ${taskSendParams.id}")
        return lock.withLock {
            val task = tasks[taskSendParams.id]
            if (task == null) {
                val newTask = Task(
                    id = taskSendParams.id,
                    sessionId = taskSendParams.sessionId,
                    status = TaskStatus(state = TaskState.submitted),
                    history = mutableListOf(taskSendParams.message)
                )
                tasks[taskSendParams.id] = newTask
                newTask
            } else {
                // Create a new task with updated history
                val updatedHistory = task.history?.toMutableList() ?: mutableListOf()
                updatedHistory.add(taskSendParams.message)

                val updatedTask = task.copy(
                    history = updatedHistory
                )
                tasks[taskSendParams.id] = updatedTask
                updatedTask
            }
        }
    }

    override suspend fun onResubscribeToTask(request: TaskResubscriptionRequest): Flow<SendTaskStreamingResponse> {
        log.info("Resubscribing to task ${request.params.id}")
        val taskQueryParams: TaskQueryParams = request.params

        return try {
            lock.withLock {
                val task = tasks[taskQueryParams.id]
                if (task == null) {
                    return flow {
                        emit(SendTaskStreamingResponse(
                            id = request.id,
                            error = TaskNotFoundError()
                        ))
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
                emit(SendTaskStreamingResponse(
                    id = request.id,
                    error = InternalError()
                ))
            }
        }
    }

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

    private fun appendTaskHistory(task: Task, historyLength: Int?): Task {
        val updatedHistory = if (historyLength != null && historyLength > 0) {
            task.history?.let { history ->
                history.takeLast(historyLength).toMutableList()
            } ?: mutableListOf()
        } else {
            mutableListOf()
        }
        return task.copy(history = updatedHistory)
    }

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

    private suspend fun enqueueEventsForSse(taskId: String, taskUpdateEvent: Any) {
        subscriberLock.withLock {
            taskSseSubscribers[taskId]?.forEach { subscriber ->
                subscriber.send(taskUpdateEvent)
            }
        }
    }

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
