package io.asterixorobelix.afrikaburn.data.datasource

import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType

interface JsonResourceDataSource {
    suspend fun loadProjectsByType(type: ProjectType): List<ProjectItem>
}