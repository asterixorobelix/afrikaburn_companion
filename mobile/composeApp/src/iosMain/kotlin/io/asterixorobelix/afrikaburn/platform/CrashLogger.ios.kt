package io.asterixorobelix.afrikaburn.platform

import platform.Foundation.NSLog

/**
 * iOS implementation using NSLog for basic crash logging
 * For production apps, consider integrating Firebase Crashlytics iOS SDK
 * or using alternative crash reporting solutions like Bugsnag
 */
class IOSCrashLogger : CrashLogger {
    
    override fun initialize() {
        // Basic initialization
        log("iOS Crash Logger initialized")
    }
    
    override fun logException(throwable: Throwable, message: String?) {
        val logMessage = buildString {
            append("EXCEPTION: ")
            message?.let { append("$it - ") }
            append("${throwable::class.simpleName}: ${throwable.message}")
            throwable.stackTraceToString().let { stack ->
                if (stack.isNotEmpty()) {
                    append("\nStack trace:\n$stack")
                }
            }
        }
        NSLog(logMessage)
    }
    
    override fun setCustomKey(key: String, value: String) {
        NSLog("CUSTOM_KEY: $key = $value")
    }
    
    override fun setUserId(userId: String) {
        NSLog("USER_ID: $userId")
    }
    
    override fun log(message: String) {
        NSLog("LOG: $message")
    }

    @Suppress("UseCheckOrError")
    override fun testCrash() {
        NSLog("TEST_CRASH: Triggering test crash")
        throw IllegalStateException("Test crash for iOS")
    }
}

/**
 * Create iOS-specific crash logger
 */
actual fun createCrashLogger(): CrashLogger = IOSCrashLogger()