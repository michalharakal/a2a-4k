// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k.notifications

import io.github.a2a_4k.models.PushNotificationConfig
import io.github.a2a_4k.models.Task

/**
 * Interface for publishing notifications about tasks to external systems.
 * Implementations of this interface handle the delivery of task information
 * to specified endpoints using the provided configuration.
 */
interface NotificationPublisher {

    /**
     * Publishes a notification about a task to the specified endpoint.
     *
     * @param task The task to publish a notification about, containing its ID, status, and other details.
     * @param config The configuration for the push notification, including the target URL and authentication details.
     */
    suspend fun publish(task: Task, config: PushNotificationConfig)
}
