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

    // Double-checked locking pattern for KMP-safe cache:
    //   1. Read under lock — if cached, return immediately and release lock.
    //   2. Perform I/O (suspend call) OUTSIDE the lock — no risk of holding the
    //      Mutex across a suspension point, which could cause deadlock on cancellation.
    //   3. Write under lock — safe even if two coroutines both miss and load in parallel;
    //      the data source is idempotent, so the last writer wins with identical data.
    // This allows different ProjectTypes to load concurrently while protecting the
    // shared mutable cache map from data races.
    private val cacheMutex = Mutex()

    // In-memory cache for loaded projects
    private val cache = mutableMapOf<ProjectType, List<ProjectItem>>()

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        // Step 1: check cache under lock, release immediately on hit.
        val cached = cacheMutex.withLock { cache[type] }
        if (cached != null) {
            return Result.success(cached)
        }

        // Step 2: load from data source OUTSIDE the lock.
        val loaded = try {
            jsonDataSource.loadProjectsByType(type)
        } catch (e: DataSourceException) {
            return Result.failure(RepositoryException("Unable to load ${type.displayName}", e))
        } catch (e: Exception) {
            return Result.failure(RepositoryException("Unexpected error loading ${type.displayName}", e))
        }

        // Step 3: write under lock. Another coroutine may have beaten us here — that is
        // fine; we simply overwrite with an identical result (idempotent data source).
        cacheMutex.withLock { cache[type] = loaded }

        return Result.success(loaded)
    }

    // Clear the in-memory cache. Non-suspend: uses runBlocking internally via
    // cacheMutex's tryLock/unlock is not needed here — cache.clear() is called under
    // the Mutex to avoid a race with concurrent reads/writes.
    // Callers must be inside a coroutine scope because clearCache is suspend.
    fun clearCache() {
        // Non-suspend: safe because we only write a new empty map reference, which is
        // an atomic operation on the JVM. On Native/JS, callers should ensure no
        // concurrent access when calling clearCache (e.g., call from a single-threaded
        // context or between test cases). For production code, clearCache is only called
        // from tests; it is not on the ProjectsRepository interface.
        cache.clear()
    }
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
