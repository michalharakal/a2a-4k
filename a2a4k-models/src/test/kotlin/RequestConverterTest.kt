// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.serialization.SerializationException

class RequestConverterTest {

    private val converter = RequestConverter()

    @Test
    fun `test fromJson with valid CancelTaskRequest`() {
        // Given
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "123",
                "method": "tasks/cancel",
                "params": {
                    "id": "task-123",
                    "metadata": {}
                }
            }
        """.trimIndent()

        // When
        val result = converter.fromJson(json)

        // Then
        assertIs<CancelTaskRequest>(result)
        assertEquals("2.0", result.jsonrpc)
        assertEquals("123", result.id)
        assertEquals("tasks/cancel", result.method)
        assertEquals("task-123", result.params.id)
    }

    @Test
    fun `test fromJson with valid GetTaskRequest`() {
        // Given
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "456",
                "method": "tasks/get",
                "params": {
                    "id": "task-456",
                    "historyLength": 10,
                    "metadata": {}
                }
            }
        """.trimIndent()

        // When
        val result = converter.fromJson(json)

        // Then
        assertIs<GetTaskRequest>(result)
        assertEquals("2.0", result.jsonrpc)
        assertEquals("456", result.id)
        assertEquals("tasks/get", result.method)
        assertEquals("task-456", result.params.id)
        assertEquals(10, result.params.historyLength)
    }

    @Test
    fun `test fromJson with unsupported method`() {
        // Given
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "789",
                "method": "unsupported_method",
                "params": {}
            }
        """.trimIndent()

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            converter.fromJson(json)
        }
    }

    @Test
    fun `test fromJson with missing method field`() {
        // Given
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "789",
                "params": {}
            }
        """.trimIndent()

        // When/Then
        val exception = assertThrows<IllegalArgumentException> {
            converter.fromJson(json)
        }
    }

    @Test
    fun `test fromJson with invalid JSON`() {
        // Given
        val json = """
            {
                "jsonrpc": "2.0",
                "id": "789",
                "method": "tasks/get",
                "params": {
                    "id": "task-789"
                },
            }
        """.trimIndent()

        // When/Then
        assertThrows<IllegalArgumentException> {
            converter.fromJson(json)
        }
    }
}
