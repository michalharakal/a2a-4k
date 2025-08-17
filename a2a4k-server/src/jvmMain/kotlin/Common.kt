// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.github.a2a_4k.models.PushNotificationConfig
import io.github.a2a_4k.models.Task
import kotlinx.coroutines.channels.Channel
import java.util.Collections.synchronizedList
import java.util.concurrent.ConcurrentHashMap

/**
 * Provides JVM-specific implementations of thread-safe collections and utility functions
 * for use in a multiplatform context. This includes factories for thread-safe maps and lists
 * to store tasks, notification configurations, and channels.
 *
 * The implementations use Java's concurrent collections to ensure thread safety on the JVM.
 *
 * Functions:
 * - createSafeTaskMap: Returns a thread-safe mutable map for tasks.
 * - createSafeNotificationConfigMap: Returns a thread-safe mutable map for notification configs.
 * - createSafeChannelMap: Returns a thread-safe mutable map for channel lists.
 * - createSafeChannelList: Returns a thread-safe mutable list of channels.
 */
actual fun createSafeTaskMap(): MutableMap<String, Task> = ConcurrentHashMap<String, Task>()

actual fun createSafeNotificationConfigMap(): MutableMap<String, PushNotificationConfig> = ConcurrentHashMap()

actual fun createSafeChannelMap(): MutableMap<String, MutableList<Channel<Any>>> = ConcurrentHashMap()

actual fun createSafeChannelList(): MutableList<Channel<Any>> = synchronizedList(mutableListOf())
