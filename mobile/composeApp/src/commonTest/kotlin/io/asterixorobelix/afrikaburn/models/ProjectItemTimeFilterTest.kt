package io.asterixorobelix.afrikaburn.models

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProjectItemTimeFilterTest {
    
    @Test
    fun `isDaytime should return true for Day Time status`() {
        // Given project with "Day Time" status
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Fam • Day Time"
        )
        
        // Then should be daytime
        assertTrue(project.isDaytime)
    }
    
    @Test
    fun `isDaytime should return true for Morning status`() {
        // Given project with "Morning" status
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Fam • Morning"
        )
        
        // Then should be daytime
        assertTrue(project.isDaytime)
    }
    
    @Test
    fun `isDaytime should return true for both Day Time and Morning`() {
        // Given project with both time indicators
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Fam • Morning, Day Time, Night Time"
        )
        
        // Then should be daytime
        assertTrue(project.isDaytime)
    }
    
    @Test
    fun `isNighttime should return true for Night Time status`() {
        // Given project with "Night Time" status
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Fam • Night Time"
        )
        
        // Then should be nighttime
        assertTrue(project.isNighttime)
    }
    
    @Test
    fun `isNighttime should return true for All Night status`() {
        // Given project with "All Night" status
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "All Night"
        )
        
        // Then should be nighttime
        assertTrue(project.isNighttime)
    }
    
    @Test
    fun `isNighttime should return true for both Night Time and All Night`() {
        // Given project with both nighttime indicators
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Day Time, Night Time, All Night"
        )
        
        // Then should be nighttime
        assertTrue(project.isNighttime)
    }
    
    @Test
    fun `project can be both daytime and nighttime`() {
        // Given project operating both day and night
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Fam • Day Time, Night Time, All Night"
        )
        
        // Then should be both daytime and nighttime
        assertTrue(project.isDaytime)
        assertTrue(project.isNighttime)
    }
    
    @Test
    fun `isDaytime should return false for night-only status`() {
        // Given project with only night operations
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Night Time, All Night"
        )
        
        // Then should not be daytime
        assertFalse(project.isDaytime)
    }
    
    @Test
    fun `isNighttime should return false for day-only status`() {
        // Given project with only day operations
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Fam • Day Time, Morning"
        )
        
        // Then should not be nighttime
        assertFalse(project.isNighttime)
    }
    
    @Test
    fun `time detection should be case insensitive`() {
        // Given project with different case variations
        val projectUpperCase = ProjectItem(
            name = "Test Camp 1",
            description = "Test description",
            status = "DAY TIME, NIGHT TIME"
        )
        
        val projectMixedCase = ProjectItem(
            name = "Test Camp 2",
            description = "Test description",
            status = "day time, night time"
        )
        
        // Then both should detect times correctly
        assertTrue(projectUpperCase.isDaytime)
        assertTrue(projectUpperCase.isNighttime)
        assertTrue(projectMixedCase.isDaytime)
        assertTrue(projectMixedCase.isNighttime)
    }
    
    @Test
    fun `matchesTimeFilter should work with ALL filter`() {
        // Given any project
        val project = ProjectItem(
            name = "Test Camp",
            description = "Test description",
            status = "Other"
        )
        
        // When checking against ALL filter
        // Then should always match
        assertTrue(project.matchesTimeFilter(TimeFilter.ALL))
    }
    
    @Test
    fun `matchesTimeFilter should work with DAYTIME filter`() {
        // Given daytime project
        val daytimeProject = ProjectItem(
            name = "Daytime Camp",
            description = "Test description",
            status = "Fam • Day Time"
        )
        
        // And nighttime project
        val nighttimeProject = ProjectItem(
            name = "Nighttime Camp",
            description = "Test description",
            status = "Night Time"
        )
        
        // Then should match correctly
        assertTrue(daytimeProject.matchesTimeFilter(TimeFilter.DAYTIME))
        assertFalse(nighttimeProject.matchesTimeFilter(TimeFilter.DAYTIME))
    }
    
    @Test
    fun `matchesTimeFilter should work with NIGHTTIME filter`() {
        // Given daytime project
        val daytimeProject = ProjectItem(
            name = "Daytime Camp",
            description = "Test description",
            status = "Fam • Day Time"
        )
        
        // And nighttime project
        val nighttimeProject = ProjectItem(
            name = "Nighttime Camp",
            description = "Test description",
            status = "Night Time"
        )
        
        // Then should match correctly
        assertFalse(daytimeProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
        assertTrue(nighttimeProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
    }
    
    @Test
    fun `matchesTimeFilter should work with projects operating both times`() {
        // Given project operating both day and night
        val allTimeProject = ProjectItem(
            name = "All Time Camp",
            description = "Test description",
            status = "Fam • Day Time, Night Time, All Night"
        )
        
        // Then should match both filters
        assertTrue(allTimeProject.matchesTimeFilter(TimeFilter.DAYTIME))
        assertTrue(allTimeProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
        assertTrue(allTimeProject.matchesTimeFilter(TimeFilter.ALL))
    }
    
    @Test
    fun `real AfrikaBurn data examples should work correctly`() {
        // Test with actual data patterns from the JSON
        val daytimeOnlyProject = ProjectItem(
            name = "The Vagabonds",
            description = "Wellness and relaxation camp",
            status = "Fam • Day Time"
        )
        
        val nighttimeOnlyProject = ProjectItem(
            name = "Space Cowboys",
            description = "Galactic space station",
            status = "All Night"
        )
        
        val mixedTimeProject = ProjectItem(
            name = "aTypical Bar",
            description = "Serving interaction day or night",
            status = "Fam • Day Time, Night Time, All Night"
        )
        
        val otherProject = ProjectItem(
            name = "ALEGRA SPACE STATION",
            description = "Space station equivalent",
            status = "Fam • Other"
        )
        
        // Then should classify correctly
        assertTrue(daytimeOnlyProject.isDaytime)
        assertFalse(daytimeOnlyProject.isNighttime)
        
        assertFalse(nighttimeOnlyProject.isDaytime)
        assertTrue(nighttimeOnlyProject.isNighttime)
        
        assertTrue(mixedTimeProject.isDaytime)
        assertTrue(mixedTimeProject.isNighttime)
        
        assertFalse(otherProject.isDaytime)
        assertFalse(otherProject.isNighttime)
        
        // Test filter matching
        assertTrue(daytimeOnlyProject.matchesTimeFilter(TimeFilter.DAYTIME))
        assertFalse(daytimeOnlyProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
        
        assertFalse(nighttimeOnlyProject.matchesTimeFilter(TimeFilter.DAYTIME))
        assertTrue(nighttimeOnlyProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
        
        assertTrue(mixedTimeProject.matchesTimeFilter(TimeFilter.DAYTIME))
        assertTrue(mixedTimeProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
        
        // Other projects should only match ALL filter
        assertFalse(otherProject.matchesTimeFilter(TimeFilter.DAYTIME))
        assertFalse(otherProject.matchesTimeFilter(TimeFilter.NIGHTTIME))
        assertTrue(otherProject.matchesTimeFilter(TimeFilter.ALL))
    }
}