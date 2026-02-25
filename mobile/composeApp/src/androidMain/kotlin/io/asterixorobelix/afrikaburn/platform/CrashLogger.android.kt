package io.asterixorobelix.afrikaburn.platform

import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val TAG = "CrashLogger"

/**
 * Android implementation that uses Firebase Crashlytics when available, or falls back to
 * Android logging when Firebase is not configured (i.e. no google-services.json / plugin).
 *
 * Firebase is a compile-time dependency so all API calls are type-safe. Graceful degradation
 * is achieved by catching [IllegalStateException] thrown by [FirebaseCrashlytics.getInstance]
 * when FirebaseApp has not been initialised — no reflection required.
 */
class AndroidCrashLogger : CrashLogger {

    /**
     * Null when Firebase is not configured; non-null otherwise.
     * Initialised once in [initialize] to avoid repeated try/catch overhead.
     */
    private var crashlytics: FirebaseCrashlytics? = null

    override fun initialize() {
        crashlytics = try {
            FirebaseCrashlytics.getInstance().also { it.setCrashlyticsCollectionEnabled(true) }
        } catch (e: IllegalStateException) {
            Log.w(TAG, "Firebase not configured — Crashlytics unavailable, using Android logging fallback", e)
            null
        }

        if (FirebaseConfigChecker.IS_USING_DEFAULT_TEMPLATE) {
            log("WARNING: Using default Firebase template — Crashlytics disabled")
            log("Replace google-services.json with your Firebase project configuration")
        }
    }

    override fun logException(throwable: Throwable, message: String?) {
        message?.let { crashlytics?.log(it) }
        crashlytics?.recordException(throwable)
        Log.e(TAG, message ?: "Exception occurred", throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        crashlytics?.setCustomKey(key, value)
        Log.d(TAG, "Custom key: $key = $value")
    }

    override fun setUserId(userId: String) {
        crashlytics?.setUserId(userId)
        Log.d(TAG, "User ID: $userId")
    }

    override fun log(message: String) {
        crashlytics?.log(message)
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
