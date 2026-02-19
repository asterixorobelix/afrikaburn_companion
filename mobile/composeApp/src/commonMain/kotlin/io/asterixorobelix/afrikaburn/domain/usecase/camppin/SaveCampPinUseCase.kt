package io.asterixorobelix.afrikaburn.domain.usecase.camppin

import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository

class SaveCampPinUseCase(
    private val repository: UserCampPinRepository
) {
    suspend operator fun invoke(params: Params): Result<Unit> {
        return runCatching {
            repository.saveCampPin(
                latitude = params.latitude,
                longitude = params.longitude,
                name = params.name
            )
        }
    }

    data class Params(
        val latitude: Double,
        val longitude: Double,
        val name: String = "My Camp"
    )
}
