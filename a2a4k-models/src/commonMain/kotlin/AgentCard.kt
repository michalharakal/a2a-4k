// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models

import kotlinx.serialization.Serializable

@Serializable
data class AgentCard(
    val name: String,
    val description: String,
    val url: String,
    val provider: Provider? = null,
    val version: String,
    val documentationUrl: String? = null,
    val capabilities: Capabilities,
    val authentication: Authentication? = null,
    val defaultInputModes: List<String>,
    val defaultOutputModes: List<String>,
    val skills: List<Skill>,
)
