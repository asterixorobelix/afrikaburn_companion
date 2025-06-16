package io.asterixorobelix.afrikaburn

import io.asterixorobelix.afrikaburn.CONSTANTS.DEFAULT_PORT
import io.asterixorobelix.afrikaburn.CONSTANTS.SHUTDOWN_PERIOD
import io.asterixorobelix.afrikaburn.CONSTANTS.SHUTDOWN_TIMEOUT
import io.ktor.server.application.Application
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.asterixorobelix.afrikaburn.plugins.configureDatabases
import io.asterixorobelix.afrikaburn.plugins.configureHTTP
import io.asterixorobelix.afrikaburn.plugins.configureMonitoring
import io.asterixorobelix.afrikaburn.plugins.configureRouting
import io.asterixorobelix.afrikaburn.plugins.configureSecurity
import io.asterixorobelix.afrikaburn.plugins.configureSerialization
import io.asterixorobelix.afrikaburn.plugins.configureStatusPages

fun main() {

    val port = System.getenv("PORT")?.toInt() ?: DEFAULT_PORT
    val server = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)

    // Add shutdown hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(SHUTDOWN_PERIOD, SHUTDOWN_TIMEOUT)
    })

    server.start(wait = true)


}

object CONSTANTS {
    const val DEFAULT_PORT = 9080
    const val SHUTDOWN_PERIOD = 1000L
    const val SHUTDOWN_TIMEOUT = 5000L
}

fun Application.module() {
    configureSecurity()
    configureSerialization()
    configureDatabases()
    configureHTTP()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
}
