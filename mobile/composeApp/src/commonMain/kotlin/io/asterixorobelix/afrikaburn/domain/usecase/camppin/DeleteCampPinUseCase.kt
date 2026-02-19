package io.asterixorobelix.afrikaburn.domain.usecase.camppin

import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository

class DeleteCampPinUseCase(
    private val repository: UserCampPinRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return runCatching { repository.deleteCampPin() }
    }
}
