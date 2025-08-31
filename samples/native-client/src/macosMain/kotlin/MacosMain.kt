// SPDX-FileCopyrightText: 2025
//
// SPDX-License-Identifier: Apache-2.0

import kotlinx.coroutines.runBlocking

/**
 * macOS entry point for the native A2A client.
 * Accepts an optional server URL as command line argument.
 */
fun main(args: Array<String>) = runBlocking {
    println("=== A2A Native Client for macOS ===")
    val serverUrl = args.firstOrNull() ?: "http://localhost:5000"
    
    if (args.isNotEmpty()) {
        println("Using custom server URL: $serverUrl")
    } else {
        println("Using default server URL: $serverUrl")
        println("Tip: Pass server URL as argument, e.g., ./client http://192.168.1.100:5000")
    }
    
    ClientApp().run(serverUrl)
}