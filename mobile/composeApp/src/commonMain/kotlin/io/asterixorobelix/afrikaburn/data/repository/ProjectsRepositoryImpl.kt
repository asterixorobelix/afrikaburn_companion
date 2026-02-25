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
        return try {
            // Check cache first (lock-protected read)
            cacheMutex.withLock { cache[type] }?.let { cachedProjects ->
                return Result.success(cachedProjects)
            }

            // Load from data source — outside the lock so I/O doesn't serialise all coroutines.
            // Worst case on first concurrent call: two coroutines both miss the cache and both
            // load from the data source (idempotent), then the last writer wins in the map.
            val projects = jsonDataSource.loadProjectsByType(type)

            // Cache the results (lock-protected write)
            cacheMutex.withLock { cache[type] = projects }

            Result.success(projects)
        } catch (e: DataSourceException) {
            Result.failure(RepositoryException("Unable to load ${type.displayName}", e))
        } catch (e: Exception) {
            Result.failure(RepositoryException("Unexpected error loading ${type.displayName}", e))
        }
    }

    // Clear cache — suspend so it can safely acquire the mutex
    suspend fun clearCache() {
        cacheMutex.withLock { cache.clear() }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
