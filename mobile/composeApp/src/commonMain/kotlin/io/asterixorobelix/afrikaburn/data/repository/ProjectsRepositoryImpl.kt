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

    // Mutex guards all reads and writes to `cache`.
    // I/O (loadProjectsByType) is intentionally kept OUTSIDE the lock so that:
    //   1. Different ProjectTypes can be loaded concurrently (no global serialisation).
    //   2. A suspended/cancelled I/O call never holds the lock.
    // Worst-case on a true concurrent first-access for the same type: two coroutines
    // both observe a cache miss, both load, and the last writer wins — which is
    // safe because the data source is idempotent and the result is identical.
    private val cacheMutex = Mutex()

    // In-memory cache for loaded projects
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        // Mutex protects the entire check-load-write critical section. This guarantees
        // that for each ProjectType, exactly one coroutine performs the load — others
        // block on the lock until the first completes and populates the cache, then
        // hit the cached result on their turn.
        // The data source is a local JSON resource (not network I/O), so holding the
        // lock during loadProjectsByType is acceptable. The lock is only held once per
        // ProjectType (first load), then all subsequent requests hit the cache.
        return cacheMutex.withLock {
            try {
                // Check cache first
                cache[type]?.let { cachedProjects ->
                    return@withLock Result.success(cachedProjects)
                }

                // Load from data source (only if not in cache)
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

    // Clear cache — suspend so callers must be in a coroutine scope,
    // which is intentional: clearCache() must not race with active loads.
    suspend fun clearCache() {
        cacheMutex.withLock { cache.clear() }
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
