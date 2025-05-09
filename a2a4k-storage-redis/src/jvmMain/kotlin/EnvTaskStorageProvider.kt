// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package io.github.a2a_4k

import io.lettuce.core.RedisURI
import io.lettuce.core.cluster.RedisClusterClient
import io.github.a2a_4k.storage.TaskStorage
import io.github.a2a_4k.storage.TaskStorageProvider

/**
 */
class EnvTaskStorageProvider : TaskStorageProvider {

    override fun provide(): TaskStorage? {
        val username = getEnv("A2A_STORAGE_REDIS_USERNAME")
        val password = getEnv("A2A_STORAGE_REDIS_PASSWORD")
        val host = getEnv("A2A_STORAGE_REDIS_HOST")
        val port = getEnv("A2A_STORAGE_REDIS_PORT")
        val ssl = getEnv("A2A_STORAGE_REDIS_SSL") == "true"
        val tls = getEnv("A2A_STORAGE_REDIS_TLS") == "true"

        if (host.isNullOrEmpty()) return null

        val url =
            RedisURI.Builder.redis(host)
                .apply {
                    if (!port.isNullOrEmpty()) withPort(port.toInt())
                }
                .withSsl(ssl)
                .withStartTls(tls)
                .apply {
                    if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
                        withAuthentication(username, password)
                    }
                }.build()
        val client = RedisClusterClient.create(url)
        return RedisTaskStorage(client)
    }

    private fun getEnv(key: String): String? {
        return System.getProperty(key) ?: System.getenv(key)
    }
}
