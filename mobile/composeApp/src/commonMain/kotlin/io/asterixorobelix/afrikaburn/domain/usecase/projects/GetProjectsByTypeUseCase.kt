package io.asterixorobelix.afrikaburn.domain.usecase.projects

import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType

class GetProjectsByTypeUseCase(
    private val repository: ProjectsRepository
) {
    suspend operator fun invoke(params: Params): Result<List<ProjectItem>> {
        return repository.getProjectsByType(params.type)
    }

    data class Params(
        val type: ProjectType
    )
}
