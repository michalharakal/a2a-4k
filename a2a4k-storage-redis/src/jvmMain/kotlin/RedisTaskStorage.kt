// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.lettuce.core.AbstractRedisClient
import io.lettuce.core.RedisClient
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.a2a4k.models.PushNotificationConfig
import org.a2a4k.models.Task
import org.a2a4k.storage.TaskStorage

/**
 * Redis implementation of the TaskStorage interface.
 * Stores tasks and their associated push notification configurations in Redis.
 * This implementation is thread-safe and persists data in Redis.
 *
 * @param redisClient The Redis client to use for connecting to Redis
 */
class RedisTaskStorage(private val redisClient: AbstractRedisClient) : TaskStorage {

    private val taskPrefix = "task:"
    private val notificationPrefix = "notification:"
    private val commands = when (redisClient) {
        is RedisClusterClient -> {
            redisClient.connect().reactive()
        }

        else -> (redisClient as RedisClient).connect().reactive()
    }

    /**
     * Stores a task in Redis.
     * If a task with the same ID already exists, it will be overwritten.
     *
     * @param task The task to store
     */
    override suspend fun store(task: Task) {
        // Store the task ID in Redis
        commands.set("$taskPrefix${task.id}", task.id).awaitSingle()
    }

    /**
     * Retrieves a task by its ID from Redis.
     *
     * @param taskId The ID of the task to retrieve
     * @return The task if found, null otherwise
     */
    override suspend fun fetch(taskId: String): Task? {
        // Check if the task exists in Redis
        val storedTaskId = commands.get("$taskPrefix$taskId").awaitSingleOrNull() ?: return null

        // If the task exists, create a minimal Task object with just the ID
        return Task(
            id = storedTaskId,
            status = org.a2a4k.models.TaskStatus(
                state = org.a2a4k.models.TaskState.UNKNOWN,
            ),
        )
    }

    /**
     * Stores a push notification configuration for a task in Redis.
     * If a configuration for the same task ID already exists, it will be overwritten.
     *
     * @param taskId The ID of the task to associate with the configuration
     * @param config The push notification configuration to store
     * @throws IllegalArgumentException if the task ID does not exist in Redis
     */
    override suspend fun storeNotificationConfig(taskId: String, config: PushNotificationConfig) {
        // Check if task exists
        fetch(taskId) ?: throw IllegalArgumentException("Task not found for $taskId")

        // Store the notification URL in Redis
        commands.set("$notificationPrefix$taskId", config.url).awaitSingleOrNull()
    }

    /**
     * Retrieves a push notification configuration for a task by its ID from Redis.
     *
     * @param taskId The ID of the task whose configuration to retrieve
     * @return The push notification configuration if found, null otherwise
     */
    override suspend fun fetchNotificationConfig(taskId: String): PushNotificationConfig? {
        // Get the notification URL from Redis
        val url = commands.get("$notificationPrefix$taskId").awaitSingleOrNull() ?: return null

        // Create a minimal PushNotificationConfig with just the URL
        return PushNotificationConfig(url = url)
    }

    /**
     * Closes the Redis connection when the storage is no longer needed.
     * This method should be called when the application is shutting down.
     */
    fun close() {
        redisClient.close()
    }
}
