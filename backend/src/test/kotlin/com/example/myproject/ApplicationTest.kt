package com.example.myproject

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.*

class ApplicationTest {
    
    @Test
    fun testRoot() = testApplication {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello World!", bodyAsText())
        }
    }
    
    @Test
    fun testHealthEndpoint() = testApplication {
        client.get("/health").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            val json = Json.parseToJsonElement(responseText).jsonObject
            assertEquals("healthy", json["status"]?.jsonPrimitive?.content)
            assertNotNull(json["timestamp"])
            assertEquals("1.0.0", json["version"]?.jsonPrimitive?.content)
        }
    }
    
    @Test
    fun testApiStatus() = testApplication {
        client.get("/api/v1/status").apply {
            assertEquals(HttpStatusCode.OK, status)
            val responseText = bodyAsText()
            val json = Json.parseToJsonElement(responseText).jsonObject
            assertEquals("running", json["api"]?.jsonPrimitive?.content)
            assertEquals("1.0.0", json["version"]?.jsonPrimitive?.content)
        }
    }
    
    @Test
    fun testNotFoundEndpoint() = testApplication {
        client.get("/nonexistent").apply {
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }
}