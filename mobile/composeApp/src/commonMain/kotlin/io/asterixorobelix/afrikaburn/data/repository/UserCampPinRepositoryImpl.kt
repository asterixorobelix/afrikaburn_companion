package io.asterixorobelix.afrikaburn.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.asterixorobelix.afrikaburn.data.database.AfrikaBurnDatabase
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinData
import io.asterixorobelix.afrikaburn.domain.repository.UserCampPinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * SQLDelight-based implementation of UserCampPinRepository.
 * All database operations run on Dispatchers.IO.
 */
class UserCampPinRepositoryImpl(
    private val database: AfrikaBurnDatabase
) : UserCampPinRepository {

    private val queries get() = database.userCampPinQueries

    override fun observeCampPin(): Flow<UserCampPinData?> {
        return queries.getUserCampPin()
            .asFlow()
            .mapToOneOrNull(Dispatchers.IO)
            .map { pin ->
                pin?.let {
                    UserCampPinData(
                        id = it.id,
                        latitude = it.latitude,
                        longitude = it.longitude,
                        name = it.name,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
            }
    }

    override suspend fun getCampPin(): UserCampPinData? = withContext(Dispatchers.IO) {
        queries.getUserCampPin().executeAsOneOrNull()?.let { pin ->
            UserCampPinData(
                id = pin.id,
                latitude = pin.latitude,
                longitude = pin.longitude,
                name = pin.name,
                createdAt = pin.createdAt,
                updatedAt = pin.updatedAt
            )
        }
    }

    override suspend fun saveCampPin(
        latitude: Double,
        longitude: Double,
        name: String
    ) {
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.saveUserCampPin(
                latitude = latitude,
                longitude = longitude,
                name = name,
                createdAt = now,
                updatedAt = now
            )
        }
    }

    override suspend fun updateLocation(
        latitude: Double,
        longitude: Double
    ) {
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateLocation(
                latitude = latitude,
                longitude = longitude,
                updatedAt = now
            )
        }
    }

    override suspend fun updateName(name: String) {
        withContext(Dispatchers.IO) {
            val now = Clock.System.now().toEpochMilliseconds()
            queries.updateName(name = name, updatedAt = now)
        }
    }

    override suspend fun deleteCampPin() {
        withContext(Dispatchers.IO) {
            queries.deleteUserCampPin()
        }
    }

    override suspend fun hasCampPin(): Boolean = withContext(Dispatchers.IO) {
        queries.hasCampPin().executeAsOne()
    }
}
