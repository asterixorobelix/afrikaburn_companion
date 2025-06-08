package io.asterixorobelix.afrikaburn.plugins

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.Application
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

private const val DEFAULT_CONNECTION_TIMEOUT_MS = 30000L
private const val DEFAULT_IDLE_TIMEOUT_MS = 600000L
private const val DEFAULT_MAX_LIFETIME_MS = 1800000L

fun Application.configureDatabases() {
    val databaseUrl = System.getenv("DATABASE_URL") 
        ?: "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"
    
    val url: String
    val driver: String
    val user: String
    val password: String
    
    when {
        databaseUrl.startsWith("jdbc:postgresql") -> {
            url = databaseUrl
            driver = "org.postgresql.Driver"
            user = System.getenv("DATABASE_USER") ?: ""
            password = System.getenv("DATABASE_PASSWORD") ?: ""
        }
        else -> {
            url = databaseUrl
            driver = "org.h2.Driver"
            user = "sa"
            password = ""
        }
    }
    
    val hikariConfig = HikariConfig().apply {
        jdbcUrl = url
        driverClassName = driver
        username = user
        setPassword(password)
        maximumPoolSize = System.getenv("DB_POOL_SIZE")?.toInt() ?: 10
        connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MS
        idleTimeout = DEFAULT_IDLE_TIMEOUT_MS
        maxLifetime = DEFAULT_MAX_LIFETIME_MS
    }
    
    Database.connect(HikariDataSource(hikariConfig))
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }
