// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.github.a2a_4k.models.PushNotificationConfig
import io.github.a2a_4k.models.Task
import io.github.a2a_4k.models.a2aJson
import io.github.a2a_4k.models.fromJson
import io.github.a2a_4k.models.toJson
import io.github.a2a_4k.storage.TaskStorage
import io.lettuce.core.AbstractRedisClient
import io.lettuce.core.RedisClient
import io.lettuce.core.cluster.RedisClusterClient
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull

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
        commands.set("$taskPrefix${task.id}", a2aJson.encodeToString(task)).awaitSingle()
    }

    /**
     * Retrieves a task by its ID from Redis.
     *
     * @param taskId The ID of the task to retrieve
     * @return The task if found, null otherwise
     */
    override suspend fun fetch(taskId: String): Task? {
        return commands.get("$taskPrefix$taskId").awaitSingleOrNull()?.fromJson<Task>() ?: return null
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

        commands.set("$notificationPrefix$taskId", config.toJson()).awaitSingleOrNull()
    }

    /**
     * Retrieves a push notification configuration for a task by its ID from Redis.
     *
     * @param taskId The ID of the task whose configuration to retrieve
     * @return The push notification configuration if found, null otherwise
     */
    override suspend fun fetchNotificationConfig(taskId: String): PushNotificationConfig? {
        // Get the notification URL from Redis
        return commands.get("$notificationPrefix$taskId").awaitSingleOrNull()?.fromJson<PushNotificationConfig>()
            ?: return null
    }

    /**
     * Closes the Redis connection when the storage is no longer needed.
     * This method should be called when the application is shutting down.
     */
    fun close() {
        redisClient.close()
    }
}
