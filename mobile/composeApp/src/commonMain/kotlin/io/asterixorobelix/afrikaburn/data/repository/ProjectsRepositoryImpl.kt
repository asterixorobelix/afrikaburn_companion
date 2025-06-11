package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.data.datasource.DataSourceException
import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.delay

class ProjectsRepositoryImpl(
    private val jsonDataSource: JsonResourceDataSource
) : ProjectsRepository {

    // In-memory cache for loaded projects
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return try {
            // Check cache first
            cache[type]?.let { cachedProjects ->
                return Result.success(cachedProjects)
            }

            // Load from data source
            val projects = jsonDataSource.loadProjectsByType(type)
            
            // Cache the results
            cache[type] = projects
            
            Result.success(projects)
        } catch (e: DataSourceException) {
            Result.failure(RepositoryException("Unable to load ${type.displayName}", e))
        } catch (e: Exception) {
            Result.failure(RepositoryException("Unexpected error loading ${type.displayName}", e))
        }
    }

    // Clear cache method for testing or force refresh
    fun clearCache() {
        cache.clear()
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)