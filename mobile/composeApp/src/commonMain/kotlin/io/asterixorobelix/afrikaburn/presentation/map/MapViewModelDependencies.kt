package io.asterixorobelix.afrikaburn.presentation.map

import io.asterixorobelix.afrikaburn.domain.usecase.camppin.DeleteCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.ObserveCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.SaveCampPinUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.camppin.UpdateCampPinLocationUseCase
import io.asterixorobelix.afrikaburn.domain.usecase.projects.GetAllProjectsUseCase
import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.LocationService

/**
 * Bundles dependencies to keep MapViewModel construction lean for DI and testing.
 */
data class MapUseCases(
    val observeCampPin: ObserveCampPinUseCase,
    val saveCampPin: SaveCampPinUseCase,
    val updateCampPinLocation: UpdateCampPinLocationUseCase,
    val deleteCampPin: DeleteCampPinUseCase,
    val getAllProjects: GetAllProjectsUseCase
)

data class MapServices(
    val locationService: LocationService,
    val crashLogger: CrashLogger
)
