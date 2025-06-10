package io.asterixorobelix.afrikaburn.data.datasource

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import afrikaburn.composeapp.generated.resources.Res

class JsonResourceDataSourceImpl : JsonResourceDataSource {

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem> {
        return try {
            val fileContent = Res.readBytes("files/${type.fileName}").decodeToString()
            Json.decodeFromString<List<ProjectItem>>(fileContent)
        } catch (e: Exception) {
            throw DataSourceException("Failed to load ${type.displayName}: ${e.message}", e)
        }
    }
}

class DataSourceException(message: String, cause: Throwable? = null) : Exception(message, cause)