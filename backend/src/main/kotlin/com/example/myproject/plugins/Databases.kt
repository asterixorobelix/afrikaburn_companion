package com.example.myproject.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

fun Application.configureDatabases() {
    val databaseUrl = System.getenv("DATABASE_URL") 
        ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    
    val (url, driver, user, password) = when {
        databaseUrl.startsWith("jdbc:postgresql") -> {
            listOf(
                databaseUrl, 
                "org.postgresql.Driver", 
                System.getenv("DATABASE_USER") ?: "", 
                System.getenv("DATABASE_PASSWORD") ?: ""
            )
        }
        else -> listOf(databaseUrl, "org.h2.Driver", "sa", "")
    }
    
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        setPassword(password)
        maximumPoolSize = System.getenv("DB_POOL_SIZE")?.toInt() ?: 10
        connectionTimeout = 30000
        idleTimeout = 600000
        maxLifetime = 1800000
    }
    
    Database.connect(HikariDataSource(hikariConfig))
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }