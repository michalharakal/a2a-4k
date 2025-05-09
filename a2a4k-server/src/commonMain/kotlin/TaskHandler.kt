// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.github.a2a_4k.models.*
import io.ktor.util.*
import kotlinx.coroutines.*

/**
 * Interface for handling tasks in the A2A system.
 */
interface TaskHandler {

    fun handle(task: Task): Task
}
