package io.asterixorobelix.afrikaburn.di

import io.asterixorobelix.afrikaburn.models.ProjectType
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class KoinComposeTest {

    @Test
    fun projectTabViewModelKey_isDistinctPerProjectType() {
        val artKey = projectTabViewModelKey(ProjectType.ART)
        val vehiclesKey = projectTabViewModelKey(ProjectType.VEHICLES)

        assertNotEquals(artKey, vehiclesKey)
        assertTrue(artKey.contains(ProjectType.ART.name))
        assertTrue(vehiclesKey.contains(ProjectType.VEHICLES.name))
    }
}
