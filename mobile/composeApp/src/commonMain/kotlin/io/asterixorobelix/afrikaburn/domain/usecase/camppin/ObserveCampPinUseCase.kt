package io.asterixorobelix.afrikaburn.domain.usecase.camppin

import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinData
import kotlinx.coroutines.flow.Flow

class ObserveCampPinUseCase(
    private val repository: UserCampPinRepository
) {
    operator fun invoke(): Flow<UserCampPinData?> {
        return repository.observeCampPin()
    }
}
