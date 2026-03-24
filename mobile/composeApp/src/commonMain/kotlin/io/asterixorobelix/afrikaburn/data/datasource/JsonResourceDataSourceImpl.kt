package io.asterixorobelix.afrikaburn.data.datasource

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import afrikaburn.composeapp.generated.resources.Res

class JsonResourceDataSourceImpl : JsonResourceDataSource {

    @OptIn(ExperimentalResourceApi::class)
    @Suppress("TooGenericExceptionCaught")
    override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
        return try {
            val fileContent = Res.readBytes("files/${type.fileName}").decodeToString()
            val sanitized = sanitizeForSkia(fileContent)
            Json.decodeFromString<List<ProjectItem>>(sanitized)
        } catch (e: kotlinx.serialization.SerializationException) {
            throw DataSourceException("Failed to parse JSON for ${type.displayName}: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw DataSourceException("Invalid resource path for ${type.displayName}: ${e.message}", e)
        } catch (e: RuntimeException) {
            throw DataSourceException("Failed to load ${type.displayName}: ${e.message}", e)
        }
    }
}

private fun sanitizeForSkia(text: String): String {
    val sb = StringBuilder(text.length)
    for (char in text) {
        val cp = char.code
        // Skip zero-width and invisible formatting characters that crash Skia
        if (cp in 0x200B..0x200F || cp in 0x202A..0x202E ||
            cp in 0x2060..0x2064 || cp == 0xFEFF
        ) continue
        sb.append(char)
    }
    return sb.toString()
}

class DataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)