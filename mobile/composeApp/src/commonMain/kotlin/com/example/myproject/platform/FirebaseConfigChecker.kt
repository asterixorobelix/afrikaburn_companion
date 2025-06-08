package com.example.myproject.platform

/**
 * Utility to check if Firebase is properly configured
 */
object FirebaseConfigChecker {
    
    /**
     * Whether we're using the default template configuration
     * True if using default template (Firebase not properly configured)
     */
    const val IS_USING_DEFAULT_TEMPLATE = true
    
    /**
     * Get a warning message if using default configuration
     */
    fun getConfigurationWarning(): String? {
        return if (IS_USING_DEFAULT_TEMPLATE) {
            "⚠️ Using default Firebase template. Crashlytics disabled. " +
            "Replace google-services.json with your Firebase project configuration."
        } else {
            null
        }
    }
    
    /**
     * Log Firebase configuration status
     */
    fun logConfigurationStatus(crashLogger: CrashLogger) {
        val warning = getConfigurationWarning()
        if (warning != null) {
            crashLogger.log("Firebase Config Warning: $warning")
        } else {
            crashLogger.log("Firebase properly configured")
        }
    }
}