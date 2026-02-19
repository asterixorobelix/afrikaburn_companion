package io.asterixorobelix.afrikaburn.domain.usecase.projects

import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.test.FakeProjectsRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetProjectsByTypeUseCaseTest {

    private val sampleProjects = listOf(
        ProjectItem(
            name = "Camp A",
            description = "Desc A",
            artist = Artist("Artist A"),
            code = "A1",
            status = "Confirmed"
        )
    )

    @Test
    fun `returns repository results for requested type`() = runTest {
        val repository = FakeProjectsRepository(
            results = mapOf(ProjectType.CAMPS to Result.success(sampleProjects))
        )
        val useCase = GetProjectsByTypeUseCase(repository)

        val result = useCase(GetProjectsByTypeUseCase.Params(ProjectType.CAMPS))

        assertTrue(result.isSuccess)
        assertEquals(sampleProjects, result.getOrThrow())
    }
}
