package com.example.myproject.platform

import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Android implementation using Firebase Crashlytics
 */
class AndroidCrashLogger : CrashLogger {
    private val crashlytics = FirebaseCrashlytics.getInstance()
    
    override fun initialize() {
        // Firebase Crashlytics is automatically initialized
        // Additional configuration can be added here if needed
        crashlytics.setCrashlyticsCollectionEnabled(true)
        
        // Log warning if using default template
        if (FirebaseConfigChecker.IS_USING_DEFAULT_TEMPLATE) {
            log("⚠️ WARNING: Using default Firebase template - Crashlytics disabled")
            log("Replace google-services.json with your Firebase project configuration")
        }
    }
    
    override fun logException(throwable: Throwable, message: String?) {
        message?.let { 
            crashlytics.log(it)
        }
        crashlytics.recordException(throwable)
    }
    
    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
    }
    
    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
    }
    
    override fun log(message: String) {
        crashlytics.log(message)
    }
    
    override fun testCrash() {
        throw IllegalStateException("Test crash for Firebase Crashlytics")
    }
}

/**
 * Create Android-specific crash logger
 */
actual fun createCrashLogger(): CrashLogger = AndroidCrashLogger()