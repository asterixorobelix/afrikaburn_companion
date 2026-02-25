package io.asterixorobelix.afrikaburn.platform

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val TAG = "CrashLogger"

/**
 * Android implementation using Firebase Crashlytics directly (compile-time dependency).
 *
 * Crashlytics only reports to Firebase when a valid google-services.json is present
 * and the google-gms / firebase-crashlytics plugins are applied (see build.gradle.kts).
 * Without those plugins the SDK silently discards all calls, so no reflection is needed.
 */
class AndroidCrashLogger : CrashLogger {

    private val crashlytics: FirebaseCrashlytics by lazy {
        FirebaseCrashlytics.getInstance()
    }

    override fun initialize() {
        crashlytics.setCrashlyticsCollectionEnabled(true)

        if (FirebaseConfigChecker.IS_USING_DEFAULT_TEMPLATE) {
            log("⚠️ WARNING: Using default Firebase template — Crashlytics disabled")
            log("Replace google-services.json with your Firebase project configuration")
        }
    }

    override fun logException(throwable: Throwable, message: String?) {
        if (message != null) {
            crashlytics.log(message)
        }
        crashlytics.recordException(throwable)
        Log.e(TAG, message ?: "Exception occurred", throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics.setCustomKey(key, value)
        Log.d(TAG, "Custom key: $key = $value")
    }

    override fun setUserId(userId: String) {
        crashlytics.setUserId(userId)
        Log.d(TAG, "User ID: $userId")
    }

    override fun log(message: String) {
        crashlytics.log(message)
        Log.d(TAG, message)
    }

    @Suppress("UseCheckOrError")
    override fun testCrash() {
        throw IllegalStateException("Test crash for Firebase Crashlytics")
    }
}

/**
 * Create Android-specific crash logger.
 */
actual fun createCrashLogger(): CrashLogger = AndroidCrashLogger()
