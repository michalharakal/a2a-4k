// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.*
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.slf4j.LoggerFactory
import org.a2a4k.models.AgentCard
import org.a2a4k.models.*
import org.a2a4k.models.JsonRpcResponse

class A2AServer(
    private val host: String = "0.0.0.0",
    private val port: Int = 5000,
    private val endpoint: String = "/",
    private val agentCard: AgentCard,
    private val taskManager: TaskManager
) {
    private val logger = LoggerFactory.getLogger(A2AServer::class.java)
    private val json = Json { ignoreUnknownKeys = true }
    private val converter = RequestConverter()

    fun start() {
        embeddedServer(Netty, port = port, host = host) {
            module()
        }.start(wait = true)
    }

    private fun Application.module() {
        routing {
            post(endpoint) {
                try {
                    val body = call.receiveText()
                    val result = when (val jsonRpcRequest = converter.fromJson(body)) {
                        is GetTaskRequest -> taskManager.onGetTask(jsonRpcRequest)
                        is SendTaskRequest -> taskManager.onSendTask(jsonRpcRequest)
                        is SendTaskStreamingRequest -> taskManager.onSendTaskSubscribe(jsonRpcRequest)
                        is CancelTaskRequest -> taskManager.onCancelTask(jsonRpcRequest)
                        is SetTaskPushNotificationRequest -> taskManager.onSetTaskPushNotification(jsonRpcRequest)
                        is GetTaskPushNotificationRequest -> taskManager.onGetTaskPushNotification(jsonRpcRequest)
                        is TaskResubscriptionRequest -> taskManager.onResubscribeToTask(jsonRpcRequest)
                        else -> throw IllegalArgumentException("Unexpected request type: ${jsonRpcRequest::class.java}")
                    }

                    createResponse(call, result)

                } catch (e: Exception) {
                    handleException(call, e)
                }
            }

            get("/.well-known/agent.json") {
                call.respond(HttpStatusCode.OK, json.encodeToJsonElement(agentCard))
            }
        }
    }

    private suspend fun createResponse(call: ApplicationCall, result: Any) {
        when (result) {
            is Flow<*> -> {
               error("TODO")

            }
            is JsonRpcResponse -> {
                call.respond(HttpStatusCode.OK, json.encodeToJsonElement(result))
            }
            else -> {
                logger.error("Unexpected result type: ${result::class.java}")
                throw IllegalArgumentException("Unexpected result type: ${result::class.java}")
            }
        }
    }

    private suspend fun handleException(call: ApplicationCall, e: Exception) {
        val jsonRpcError = when (e) {
            is IllegalArgumentException -> InvalidRequestError()
            else -> {
                logger.error("Unhandled exception: $e")
                InternalError()
            }
        }

        val response = ErrorResponse(id = null, error = jsonRpcError)
        call.respond(HttpStatusCode.BadRequest, json.encodeToJsonElement(response))
    }
}
