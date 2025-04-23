// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import org.a2a4k.models.AgentCard
import org.a2a4k.models.Authentication
import org.a2a4k.models.Capabilities
import org.a2a4k.models.Skill
import org.a2a4k.models.Task

val capabilities = Capabilities(
    streaming = true,
    pushNotifications = true,
    stateTransitionHistory = true,
)

val authentication = Authentication(
    schemes = listOf("none"),
)

val skill = Skill(
    id = "test-skill",
    name = "Test Skill",
    description = "A test skill",
    tags = listOf("test"),
    examples = listOf("Example 1"),
    inputModes = listOf("text"),
    outputModes = listOf("text"),
)

val agentCard = AgentCard(
    name = "Test Agent",
    description = "Test Agent Description",
    url = "https://example.com",
    version = "1.0.0",
    capabilities = capabilities,
    authentication = authentication,
    defaultInputModes = listOf("text"),
    defaultOutputModes = listOf("text"),
    skills = listOf(skill),
)

class NoopTaskHandler : TaskHandler {
    override fun handle(task: Task): Task {
        return task
    }
}
