package com.example.myproject

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.myproject.plugins.*
import com.example.myproject.di.appModule
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun main() {
    val port = System.getenv("PORT")?.toInt() ?: 8085
    val server = embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
    
    // Add shutdown hook for graceful shutdown
    Runtime.getRuntime().addShutdownHook(Thread {
        println("Shutting down server...")
        server.stop(1000, 5000)
        println("Server stopped.")
    })
    
    server.start(wait = true)
}

fun Application.module() {
    install(Koin) {
        slf4jLogger()
        modules(appModule)
    }
    
    configureSecurity()
    configureSerialization()
    configureDatabases()
    configureHTTP()
    configureMonitoring()
    configureStatusPages()
    configureRouting()
}