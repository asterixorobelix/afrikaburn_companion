package io.asterixorobelix.afrikaburn.platform

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * Stub implementation of DeviceIdProvider for development
 */
@OptIn(ExperimentalUuidApi::class)
class DeviceIdProviderStub : DeviceIdProvider {
    private val deviceId = Uuid.random().toString()
    
    override fun getDeviceId(): String = deviceId
}