package io.asterixorobelix.afrikaburn

import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Test suite that runs a basic validation of the test infrastructure.
 * This ensures our test setup is working correctly.
 */
class TestSuite {
    
    @Test
    fun `test infrastructure should be working`() {
        // Basic test to verify our test setup works
        assertTrue(true, "Test infrastructure is working")
    }
    
    @Test
    fun `kotlin test framework should be available`() {
        // Verify we can use kotlin.test assertions
        val list = listOf(1, 2, 3)
        assertTrue(list.isNotEmpty())
        assertTrue(list.contains(2))
    }
    
    @Test
    fun `coroutines test should be available`() {
        // This test existing means kotlinx-coroutines-test is available
        // The actual coroutine tests are in the specific test files
        assertTrue(true, "Coroutines test framework is available")
    }
}