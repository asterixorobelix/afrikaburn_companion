package io.asterixorobelix.afrikaburn.test

import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType

class FakeProjectsRepository(
    private val results: Map<ProjectType, Result<List<ProjectItem>>> = emptyMap(),
    private val defaultResult: Result<List<ProjectItem>> = Result.success(emptyList())
) : ProjectsRepository {
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return results[type] ?: defaultResult
    }
}
