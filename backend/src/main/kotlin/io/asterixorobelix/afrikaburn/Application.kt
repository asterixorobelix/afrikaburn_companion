package io.asterixorobelix.afrikaburn

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
    val defaultPort = 9080
    val shutdownGracePeriod = 1000L
    val shutdownTimeout = 5000L

    val port = System.getenv("PORT")?.toInt() ?: defaultPort
    val server = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)

    // Add shutdown hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(shutdownGracePeriod, shutdownTimeout)
    })

    server.start(wait = true)
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
