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
            Json.decodeFromString<List<ProjectItem>>(fileContent)
        } catch (e: kotlinx.serialization.SerializationException) {
            throw DataSourceException("Failed to parse JSON for ${type.displayName}: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw DataSourceException("Invalid resource path for ${type.displayName}: ${e.message}", e)
        } catch (e: OutOfMemoryError) {
            throw DataSourceException("Resource too large for ${type.displayName}: ${e.message}", e)
        } catch (e: RuntimeException) {
            throw DataSourceException("Failed to load ${type.displayName}: ${e.message}", e)
        }
    }
}

class DataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)