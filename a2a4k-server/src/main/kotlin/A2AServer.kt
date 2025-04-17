// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import io.ktor.sse.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import org.a2a4k.models.AgentCard
import org.a2a4k.models.CancelTaskRequest
import org.a2a4k.models.ErrorResponse
import org.a2a4k.models.GetTaskPushNotificationRequest
import org.a2a4k.models.GetTaskRequest
import org.a2a4k.models.InternalError
import org.a2a4k.models.InvalidRequestError
import org.a2a4k.models.RequestConverter
import org.a2a4k.models.SendTaskRequest
import org.a2a4k.models.SendTaskStreamingRequest
import org.a2a4k.models.SetTaskPushNotificationRequest
import org.a2a4k.models.TaskResubscriptionRequest
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

/**
 * A2AServer implements an Agent-to-Agent communication server based on the A2A protocol.
 *
 * This server handles various JSON-RPC requests for task management, including task creation,
 * retrieval, cancellation, and notification management. It also provides an endpoint for
 * retrieving the agent's metadata (agent card).
 */
class A2AServer(
    /**
     * The hostname or IP address the server will bind to.
     * Default is "0.0.0.0" which binds to all network interfaces.
     */
    private val host: String = "0.0.0.0",

    /**
     * The port number the server will listen on.
     * Default is 5000.
     */
    private val port: Int = 5000,

    /**
     * The endpoint path for the server.
     * Default is "/".
     */
    private val endpoint: String = "/",

    /**
     * The agent card containing metadata about this agent, including its capabilities,
     * authentication requirements, and other descriptive information.
     */
    private val agentCard: AgentCard,

    /**
     * The task manager responsible for handling task-related operations such as
     * creating, retrieving, and canceling tasks.
     */
    private val taskManager: TaskManager
) {
    /**
     * Logger instance for this class.
     */
    private val log = LoggerFactory.getLogger(A2AServer::class.java)

    /**
     * JSON serializer/deserializer configured to ignore unknown keys in the input.
     */
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Converter for parsing JSON-RPC requests from their string representation.
     */
    private val converter = RequestConverter()

    private val server = AtomicReference<EmbeddedServer<*, *>>()

    /**
     * Starts the A2A server and begins listening for incoming connections.
     *
     * This method initializes a Netty server with the configured host and port,
     * installs the Server-Sent Events (SSE) feature, and configures the routing module.
     * The server starts in a blocking mode (wait = true), which means this method
     * will not return until the server is stopped.
     */
    fun start(wait: Boolean = false) {
        server.set(embeddedServer(Netty, port = port, host = host) {
            install(SSE)
            module()
        }.start(wait = wait))
    }

    fun stop() {
        server.get()?.stop(0, 0)
    }

    /**
     * Configures the application routing for the A2A server.
     *
     * This extension function sets up two main routes:
     * 1. An SSE (Server-Sent Events) endpoint that handles various JSON-RPC requests for task management
     * 2. A well-known endpoint (/.well-known/agent.json) that returns the agent's metadata card
     *
     * The SSE endpoint processes different types of requests and either responds immediately or
     * establishes a streaming connection for long-running tasks.
     *
     * @receiver The Ktor Application instance to configure
     */
    private fun Application.module() {
        routing {
            post(endpoint) {
                try {
                    val body = call.receiveText()
                    val result = when (val jsonRpcRequest = converter.fromJson(body)) {
                        is GetTaskRequest -> taskManager.onGetTask(jsonRpcRequest)
                        is SendTaskRequest -> taskManager.onSendTask(jsonRpcRequest)
                        is CancelTaskRequest -> taskManager.onCancelTask(jsonRpcRequest)
                        is SetTaskPushNotificationRequest -> taskManager.onSetTaskPushNotification(jsonRpcRequest)
                        is GetTaskPushNotificationRequest -> taskManager.onGetTaskPushNotification(jsonRpcRequest)

                        else -> null
                    }
                    if (result != null) {
                        call.respond(HttpStatusCode.OK, json.encodeToString(result))
                    } else {
                        val streamingResult = when (val jsonRpcRequest = converter.fromJson(body)) {
                            is SendTaskStreamingRequest -> taskManager.onSendTaskSubscribe(jsonRpcRequest)
                            is TaskResubscriptionRequest -> taskManager.onResubscribeToTask(jsonRpcRequest)
                            else -> throw IllegalArgumentException("Unexpected request type: ${jsonRpcRequest::class.java}")
                        }

                        sse {
                            streamingResult.collect {
                                send(ServerSentEvent(json.encodeToString(it)))
                            }
                        }
                    }
                } catch (e: Exception) {
                    handleException(call, e)
                }
            }

            get("/.well-known/agent.json") {
                call.respond(HttpStatusCode.OK, json.encodeToString(agentCard))
            }
        }
    }

    /**
     * Handles exceptions that occur during request processing and returns appropriate error responses.
     *
     * This method maps different types of exceptions to specific JSON-RPC error types:
     * - IllegalArgumentException is mapped to InvalidRequestError
     * - All other exceptions are logged and mapped to InternalError
     *
     * The method then creates an ErrorResponse and sends it back to the client with a BadRequest status code.
     *
     * @param call The ApplicationCall context for the current request
     * @param e The exception that was thrown during request processing
     */
    private suspend fun handleException(call: ApplicationCall, e: Exception) {
        log.error("Exception detected: $e")
        val jsonRpcError = when (e) {
            is IllegalArgumentException -> InvalidRequestError()
            else -> {
                log.error("Unhandled exception: $e")
                InternalError()
            }
        }

        val response = ErrorResponse(id = null, error = jsonRpcError)
        call.respond(HttpStatusCode.OK, json.encodeToString(response))
    }
}
