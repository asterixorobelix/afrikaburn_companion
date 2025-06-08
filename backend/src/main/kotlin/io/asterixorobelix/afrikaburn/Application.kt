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
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
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
