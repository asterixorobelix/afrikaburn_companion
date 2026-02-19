package io.asterixorobelix.afrikaburn.domain.usecase.camppin

import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository

class UpdateCampPinLocationUseCase(
    private val repository: UserCampPinRepository
) {
    suspend operator fun invoke(params: Params): Result<Unit> {
        return runCatching {
            repository.updateLocation(
                latitude = params.latitude,
                longitude = params.longitude
            )
        }
    }

    data class Params(
        val latitude: Double,
        val longitude: Double
    )
}
