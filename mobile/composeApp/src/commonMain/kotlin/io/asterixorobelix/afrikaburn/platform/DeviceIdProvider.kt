package io.asterixorobelix.afrikaburn.platform

/**
 * Interface for device ID provider
 */
interface DeviceIdProvider {
    fun getDeviceId(): String
}