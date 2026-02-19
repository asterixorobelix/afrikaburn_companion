package io.asterixorobelix.afrikaburn.domain.usecase.projects

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType

class GetAllProjectsUseCase(
    private val getProjectsByTypeUseCase: GetProjectsByTypeUseCase
) {
    suspend operator fun invoke(): Result<List<ProjectItem>> {
        val combined = mutableListOf<ProjectItem>()
        for (type in ProjectType.entries) {
            val result = getProjectsByTypeUseCase(GetProjectsByTypeUseCase.Params(type))
            result.onFailure { return Result.failure(it) }
            combined += result.getOrNull().orEmpty()
        }
        return Result.success(combined)
    }
}
