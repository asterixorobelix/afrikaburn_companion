package io.asterixorobelix.afrikaburn.platform

import android.build.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics

private const val TAG = "CrashLogger"
private const val MAX_KEY_LENGTH = 128
private const val MAX_VALUE_LENGTH = 1024

/**
 * Android implementation that uses Firebase Crashlytics when available, or falls back to
 * Android logging when Firebase is not configured (i.e. no google-services.json).
 *
 * Firebase is only a compile-time dependency when google-services.json is present;
 * the dependency resolution logic (eagerly in init block) gracefully handles both cases.
 *
 * IMPORTANT: initialize() must be called once at app startup before logging any crashes.
 * It resolves the Firebase Crashlytics instance and validates configuration.
 */
class AndroidCrashLogger : CrashLogger {

    /**
     * Initialised by initialize(); null when Firebase is not configured.
     * All public methods safely call against this field.
     */
    private var crashlytics: FirebaseCrashlytics? = null
    private var isInitialised = false

    override fun initialize() {
        isInitialised = true
        crashlytics = try {
            // Attempt to get Firebase Crashlytics instance.
            // Throws IllegalStateException if FirebaseApp not initialised (no google-services.json).
            FirebaseCrashlytics.getInstance().apply {
                setCrashlyticsCollectionEnabled(true)
            }
        } catch (@Suppress("SwallowedException") e: IllegalStateException) {
            // Firebase not configured; use fallback logging.
            Log.i(TAG, "Firebase Crashlytics not available (no google-services.json) — using Android Log fallback")
            null
        } catch (@Suppress("SwallowedException") e: Exception) {
            // Other initialization errors (unexpected).
            Log.e(TAG, "Error initialising Firebase Crashlytics", e)
            null
        }

        if (FirebaseConfigChecker.IS_USING_DEFAULT_TEMPLATE) {
            Log.w(TAG, "Using default Firebase template — Crashlytics features disabled")
        }
    }

    override fun logException(throwable: Throwable, message: String?) {
        ensureInitialised()
        
        // Log exception to Crashlytics (non-sensitive context only).
        // Do NOT log the message to Crashlytics if it may contain PII.
        crashlytics?.recordException(throwable)
        
        // Fallback to Android Log (safe to log message since this is for debugging).
        Log.e(TAG, message ?: "Exception occurred", throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        ensureInitialised()
        
        // Validate key/value lengths per Firebase documentation limits.
        if (key.length > MAX_KEY_LENGTH) {
            Log.w(TAG, "Custom key too long (${key.length} > $MAX_KEY_LENGTH), truncating")
            return
        }
        if (value.length > MAX_VALUE_LENGTH) {
            Log.w(TAG, "Custom value too long (${value.length} > $MAX_VALUE_LENGTH), truncating")
            return
        }
        
        // Set in Crashlytics (safe for non-sensitive values).
        crashlytics?.setCustomKey(key, value)
    }

    override fun setUserId(userId: String) {
        ensureInitialised()
        
        // Do NOT log userId to logcat — it is sensitive/PII.
        // Only set in Crashlytics if available.
        crashlytics?.setUserId(userId)
    }

    override fun log(message: String) {
        ensureInitialised()
        
        // Validate message length to prevent log buffer overflow.
        val safeMessage = if (message.length > MAX_VALUE_LENGTH) {
            message.substring(0, MAX_VALUE_LENGTH) + "... (truncated)"
        } else {
            message
        }
        
        // Log to both Crashlytics and Android Log.
        crashlytics?.log(safeMessage)
        Log.d(TAG, safeMessage)
    }

    /**
     * Force a crash for testing purposes.
     * Only available in debug builds to prevent accidental/malicious crashes in production.
     */
    @Suppress("UseCheckOrError")
    override fun testCrash() {
        ensureInitialised()
        
        if (BuildConfig.DEBUG) {
            throw IllegalStateException("Test crash for Firebase Crashlytics")
        } else {
            Log.e(TAG, "testCrash() disabled in release builds")
        }
    }

    /**
     * Ensure initialize() was called before any logging operation.
     */
    private fun ensureInitialised() {
        if (!isInitialised) {
            Log.e(TAG, "CrashLogger used before initialize() called — crashes may not be reported")
        }
    }
}

/**
 * Create Android-specific crash logger.
 * Caller must call initialize() before logging any crashes.
 */
actual fun createCrashLogger(): CrashLogger = AndroidCrashLogger()
