// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.util.*
import kotlinx.coroutines.*
import org.a2a4k.models.*

/**
 * Interface for handling tasks in the A2A system.
 */
interface TaskHandler {

    fun handle(task: Task): Task
}
