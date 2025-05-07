// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.arc

import org.a2a4k.models.Task
import org.eclipse.lmos.arc.agents.dsl.extensions.SystemContext
import org.eclipse.lmos.arc.agents.dsl.extensions.SystemContextProvider
import org.eclipse.lmos.arc.agents.dsl.extensions.UserProfile
import org.eclipse.lmos.arc.agents.dsl.extensions.UserProfileProvider

/**
 * Provides the system context and user profile to the context of the DSL.
 */
class ContextProvider(private val task: Task) : SystemContextProvider, UserProfileProvider {

    override fun provideSystem(): SystemContext {
        return SystemContext(
            task.metadata.filter { it.key.startsWith("system_") }
                .mapValues { it.value.substringAfter("system_") },
        )
    }

    override fun provideProfile(): UserProfile {
        return UserProfile(
            task.metadata.filter { it.key.startsWith("user_") }.mapValues {
                it.value.substringAfter("user_")
            },
        )
    }
}
