// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.storage.redis

import kotlinx.coroutines.runBlocking
import org.a2a4k.RedisTaskStorage
import org.a2a4k.models.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class RedisTaskStorageTest : TestBase() {

    private lateinit var storage: RedisTaskStorage

    @BeforeEach
    fun setup() {
        storage = RedisTaskStorage(createRedisClient())
    }

    @AfterEach
    fun cleanup() {
        storage.close()
    }

    @Test
    fun testStoreAndFetchTask(): Unit = runBlocking {
        // Create a test task
        val task = Task(
            id = "test-task-1",
            sessionId = "test-session",
            status = TaskStatus(
                state = TaskState.SUBMITTED,
            ),
        )

        // Store the task
        storage.store(task)

        // Fetch the task
        val fetchedTask = storage.fetch("test-task-1")

        // Verify the task was stored and retrieved correctly
        assertNotNull(fetchedTask)
        assertEquals("test-task-1", fetchedTask.id)
    }

    @Test
    fun testFetchNonExistentTask(): Unit = runBlocking {
        // Fetch a non-existent task
        val task = storage.fetch("non-existent-task")

        // Verify the task is null
        assertNull(task)
    }

    @Test
    fun testStoreAndFetchNotificationConfig(): Unit = runBlocking {
        // Create a test task
        val task = Task(
            id = "test-task-2",
            sessionId = "test-session",
            status = TaskStatus(
                state = TaskState.SUBMITTED,
            ),
        )

        // Store the task
        storage.store(task)

        // Create a test notification config
        val config = PushNotificationConfig(
            url = "https://example.com/webhook",
            token = "test-token",
        )

        // Store the notification config
        storage.storeNotificationConfig("test-task-2", config)

        // Fetch the notification config
        val fetchedConfig = storage.fetchNotificationConfig("test-task-2")

        // Verify the config was stored and retrieved correctly
        assertNotNull(fetchedConfig)
        assertEquals("https://example.com/webhook", fetchedConfig.url)
    }

    @Test
    fun testFetchNonExistentNotificationConfig(): Unit = runBlocking {
        // Create a test task
        val task = Task(
            id = "test-task-3",
            sessionId = "test-session",
            status = TaskStatus(
                state = TaskState.SUBMITTED,
            ),
        )

        // Store the task
        storage.store(task)

        // Fetch a non-existent notification config
        val config = storage.fetchNotificationConfig("test-task-3")

        // Verify the config is null
        assertNull(config)
    }
}
