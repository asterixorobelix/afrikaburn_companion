package io.asterixorobelix.afrikaburn.presentation.map

import io.asterixorobelix.afrikaburn.domain.repository.ProjectsRepository
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinData
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.DeleteCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.ObserveCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.SaveCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.UpdateCampPinLocationUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetAllProjectsUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetProjectsByTypeUseCase
import io.asterixorobelix.afrikaburn.models.Artist
import io.asterixorobelix.afrikaburn.models.ProjectItem
import io.asterixorobelix.afrikaburn.models.ProjectType
import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.LocationData
import io.asterixorobelix.afrikaburn.platform.LocationService
import io.asterixorobelix.afrikaburn.platform.PermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class MapViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadProjects success updates ui state with projects`() = runTest {
        val projects = listOf(
            ProjectItem(
                name = "Art A",
                description = "Desc",
                artist = Artist("Artist A"),
                code = "A1",
                status = "Confirmed"
            )
        )

        val repository = FakeProjectsRepository(
            results = mapOf(ProjectType.ART to Result.success(projects))
        )
        val getAllProjectsUseCase = GetAllProjectsUseCase(GetProjectsByTypeUseCase(repository))

        val viewModel = MapViewModel(
            useCases = MapUseCases(
                observeCampPin = ObserveCampPinUseCase(FakeUserCampPinRepository()),
                saveCampPin = SaveCampPinUseCase(FakeUserCampPinRepository()),
                updateCampPinLocation = UpdateCampPinLocationUseCase(FakeUserCampPinRepository()),
                deleteCampPin = DeleteCampPinUseCase(FakeUserCampPinRepository()),
                getAllProjects = getAllProjectsUseCase
            ),
            services = MapServices(
                locationService = FakeLocationService(),
                crashLogger = FakeCrashLogger()
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MapUiState.Success)
        assertEquals(projects, state.projects)
    }

    @Test
    fun `loadProjects failure updates ui state with error`() = runTest {
        val repository = FakeProjectsRepository(
            results = mapOf(ProjectType.ART to Result.failure(Exception("boom")))
        )
        val getAllProjectsUseCase = GetAllProjectsUseCase(GetProjectsByTypeUseCase(repository))

        val viewModel = MapViewModel(
            useCases = MapUseCases(
                observeCampPin = ObserveCampPinUseCase(FakeUserCampPinRepository()),
                saveCampPin = SaveCampPinUseCase(FakeUserCampPinRepository()),
                updateCampPinLocation = UpdateCampPinLocationUseCase(FakeUserCampPinRepository()),
                deleteCampPin = DeleteCampPinUseCase(FakeUserCampPinRepository()),
                getAllProjects = getAllProjectsUseCase
            ),
            services = MapServices(
                locationService = FakeLocationService(),
                crashLogger = FakeCrashLogger()
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is MapUiState.Error)
        assertEquals("boom", state.message)
    }

    @Test
    fun `camp pin updates propagate to ui state`() = runTest {
        val pinFlow = MutableStateFlow<UserCampPinData?>(null)
        val repository = FakeProjectsRepository(
            results = mapOf(ProjectType.ART to Result.success(emptyList()))
        )
        val getAllProjectsUseCase = GetAllProjectsUseCase(GetProjectsByTypeUseCase(repository))

        val viewModel = MapViewModel(
            useCases = MapUseCases(
                observeCampPin = ObserveCampPinUseCase(FakeUserCampPinRepository(pinFlow)),
                saveCampPin = SaveCampPinUseCase(FakeUserCampPinRepository()),
                updateCampPinLocation = UpdateCampPinLocationUseCase(FakeUserCampPinRepository()),
                deleteCampPin = DeleteCampPinUseCase(FakeUserCampPinRepository()),
                getAllProjects = getAllProjectsUseCase
            ),
            services = MapServices(
                locationService = FakeLocationService(),
                crashLogger = FakeCrashLogger()
            )
        )

        testDispatcher.scheduler.advanceUntilIdle()

        pinFlow.value = UserCampPinData(
            id = 1L,
            latitude = 1.0,
            longitude = 2.0,
            name = "My Camp",
            createdAt = 0L,
            updatedAt = 0L
        )
        testDispatcher.scheduler.advanceUntilIdle()

        val state = viewModel.uiState.value as MapUiState.Success
        assertTrue(state.userCampPin is CampPinState.Placed)
    }
}

private class FakeProjectsRepository(
    private val results: Map<ProjectType, Result<List<ProjectItem>>>
) : ProjectsRepository {
    override suspend fun getProjectsByType(type: ProjectType): Result<List<ProjectItem>> {
        return results[type] ?: Result.success(emptyList())
    }
}

private class FakeUserCampPinRepository(
    private val flow: Flow<UserCampPinData?> = flowOf(null)
) : UserCampPinRepository {
    override fun observeCampPin(): Flow<UserCampPinData?> = flow
    override suspend fun getCampPin(): UserCampPinData? = null
    override suspend fun saveCampPin(latitude: Double, longitude: Double, name: String) = Unit
    override suspend fun updateLocation(latitude: Double, longitude: Double) = Unit
    override suspend fun updateName(name: String) = Unit
    override suspend fun deleteCampPin() = Unit
    override suspend fun hasCampPin(): Boolean = false
}

private class FakeLocationService : LocationService {
    override suspend fun checkPermission(): PermissionState = PermissionState.NOT_DETERMINED
    override suspend fun requestPermission(): PermissionState = PermissionState.NOT_DETERMINED
    override fun startLocationUpdates(): Flow<LocationData> = flowOf()
    override fun stopLocationUpdates() = Unit
    override suspend fun getCurrentLocation(): LocationData? = null
}

private class FakeCrashLogger : CrashLogger {
    override fun initialize() = Unit
    override fun logException(throwable: Throwable, message: String?) = Unit
    override fun setCustomKey(key: String, value: String) = Unit
    override fun setUserId(userId: String) = Unit
    override fun log(message: String) = Unit
    override fun testCrash() = Unit
}
