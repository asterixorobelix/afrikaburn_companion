package io.asterixorobelix.afrikaburn.data.repository

import io.asterixorobelix.afrikaburn.data.datasource.DataSourceException
import io.asterixorobelix.afrikaburn.data.datasource.JsonResourceDataSource
import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ProjectsRepositoryImpl(
    private val jsonDataSource: JsonResourceDataSource
) : ProjectsRepository {

    // Mutex to protect cache from concurrent coroutine access (prevents duplicate loads)
    private val cacheMutex = Mutex()

    // In-memory cache for loaded projects
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return cacheMutex.withLock {
            try {
                // Check cache first
                cache[type]?.let { cachedProjects ->
                    return@withLock Result.success(cachedProjects)
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
    }

    // Clear cache method for testing or force refresh
    suspend fun clearCache() {
        cacheMutex.withLock { cache.clear() }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
