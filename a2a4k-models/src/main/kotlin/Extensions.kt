// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models


fun String.toUserMessage() = Message(
    role = "user",
    parts = listOf(TextPart(text = this))
)