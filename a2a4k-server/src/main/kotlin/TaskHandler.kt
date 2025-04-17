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

/**
 * Interface for handling tasks in the A2A system.
 */
interface TaskHandler {

     fun handle(task: Task): Task
}
