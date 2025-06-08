package com.example.myproject.platform

/**
 * Cross-platform crash logging interface
 * Provides crash reporting capabilities for both Android and iOS
 */
interface CrashLogger {
    /**
     * Initialize crash logging
     * Should be called early in app lifecycle
     */
    fun initialize()
    
    /**
     * Log a non-fatal exception
     * @param throwable The exception to log
     * @param message Optional custom message
     */
    fun logException(throwable: Throwable, message: String? = null)
    
    /**
     * Set custom key-value pair for crash context
     * @param key The key for the custom data
     * @param value The value to associate with the key
     */
    fun setCustomKey(key: String, value: String)
    
    /**
     * Set user identifier for crash reports
     * @param userId The user identifier
     */
    fun setUserId(userId: String)
    
    /**
     * Log a custom message
     * @param message The message to log
     */
    fun log(message: String)
    
    /**
     * Force a crash for testing purposes
     * Should only be used in debug builds
     */
    fun testCrash()
}

/**
 * Expected platform-specific implementation
 */
expect fun createCrashLogger(): CrashLogger