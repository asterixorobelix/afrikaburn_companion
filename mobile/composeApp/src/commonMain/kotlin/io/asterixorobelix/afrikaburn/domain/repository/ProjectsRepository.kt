package io.asterixorobelix.afrikaburn.domain.repository

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType

interface ProjectsRepository {
    suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>>
}