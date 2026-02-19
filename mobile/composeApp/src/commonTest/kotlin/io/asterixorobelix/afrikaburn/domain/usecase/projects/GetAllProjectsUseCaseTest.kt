package io.asterixorobelix.afrikaburn.domain.usecase.projects

import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.test.FakeProjectsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetAllProjectsUseCaseTest {

    @Test
    fun `combines projects across all project types`() = runTest {
        val artProjects = listOf(
            ProjectItem(
                name = "Art A",
                description = "Desc",
                artist = Artist("Artist A"),
                code = "ART1",
                status = "Confirmed"
            )
        )
        val campProjects = listOf(
            ProjectItem(
                name = "Camp A",
                description = "Desc",
                artist = Artist("Artist B"),
                code = "CAMP1",
                status = "Confirmed"
            )
        )

        val repository = FakeProjectsRepository(
            results = mapOf(
                ProjectType.ART to Result.success(artProjects),
                ProjectType.CAMPS to Result.success(campProjects)
            )
        )
        val getProjectsByTypeUseCase = GetProjectsByTypeUseCase(repository)
        val useCase = GetAllProjectsUseCase(getProjectsByTypeUseCase)

        val result = useCase()

        assertTrue(result.isSuccess)
        val combined = result.getOrThrow()
        assertEquals(artProjects + campProjects, combined.filter { it.code in setOf("ART1", "CAMP1") })
    }
}
