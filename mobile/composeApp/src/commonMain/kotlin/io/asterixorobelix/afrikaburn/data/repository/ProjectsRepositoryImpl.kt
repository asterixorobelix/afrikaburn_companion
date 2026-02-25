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

    // Single Mutex wrapping the entire check-load-write critical section.
    // Guarantees that for each ProjectType, exactly one coroutine loads from
    // the data source — all others block until the first completes, then hit
    // the populated cache. The data source is a local JSON asset (not network I/O),
    // so briefly holding the lock during the load is acceptable and results in a
    // single load per type across all concurrent callers.
    private val cacheMutex = Mutex()

    // In-memory cache for loaded projects
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return cacheMutex.withLock {
            try {
                // Return cached result if available
                cache[type]?.let { return@withLock Result.success(it) }

                // Load from data source — only one coroutine executes this per type
                val projects = jsonDataSource.loadProjectsByType(type)
                cache[type] = projects
                Result.success(projects)
            } catch (e: DataSourceException) {
                Result.failure(RepositoryException("Unable to load ${type.displayName}", e))
            } catch (e: Exception) {
                Result.failure(RepositoryException("Unexpected error loading ${type.displayName}", e))
            }
        }
    }

    // Clear the in-memory cache. Must be called from a coroutine scope.
    // Uses the same Mutex to prevent concurrent modification during an active load.
    suspend fun clearCache() {
        cacheMutex.withLock { cache.clear() }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
