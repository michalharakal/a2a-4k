// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0
package org.a2a4k.storage.redis

import com.redis.testcontainers.RedisContainer
import io.lettuce.core.RedisClient
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
open class TestBase {

    companion object {
        private val redisImage: DockerImageName = DockerImageName.parse("redis:7.0.12")

        @Container
        private val redisContainer = RedisContainer(redisImage).withExposedPorts(6379).also { it.start() }
    }

    fun createRedisClient(): RedisClient {
        val port = redisContainer.getMappedPort(6379)
        val redisURI = "redis://localhost:$port"
        return RedisClient.create(redisURI)
    }
}
