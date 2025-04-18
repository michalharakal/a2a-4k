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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.a2a4k.models.AgentCard
import org.a2a4k.models.CancelTaskRequest
import org.a2a4k.models.ErrorResponse
import org.a2a4k.models.GetTaskPushNotificationRequest
import org.a2a4k.models.GetTaskRequest
import org.a2a4k.models.InternalError
import org.a2a4k.models.InvalidRequestError
import org.a2a4k.models.JsonParseError
import org.a2a4k.models.MethodNotFoundError
import org.a2a4k.models.RequestConverter
import org.a2a4k.models.SendTaskRequest
import org.a2a4k.models.SendTaskStreamingRequest
import org.a2a4k.models.SetTaskPushNotificationRequest
import org.a2a4k.models.TaskResubscriptionRequest
import org.a2a4k.models.UnknownMethodRequest
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
    private val taskManager: TaskManager,
) {
    /**
     * Logger instance for this class.
     */
    private val log = LoggerFactory.getLogger(A2AServer::class.java)

     /**
     * The server instance that will be started.
     */
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
        server.set(
            embeddedServer(Netty, port = port, host = host) {
                a2aModule(endpoint, taskManager, agentCard)
            }.start(wait = wait),
        )
    }

    fun stop() {
        server.get()?.stop(0, 0)
    }
}
