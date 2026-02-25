package io.asterixorobelix.afrikaburn.platform

import android.os.Build
import android.util.Log
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.asterixorobelix.afrikaburn.BuildConfig

private const val TAG = "CrashLogger"
private const val MAX_KEY_LENGTH = 128
private const val MAX_VALUE_LENGTH = 1024

/**
 * Android implementation that uses Firebase Crashlytics when available, or falls back to
 * Android logging when Firebase is not configured (i.e. no google-services.json).
 *
 * Firebase is only a compile-time dependency when google-services.json is present;
 * the dependency resolution logic gracefully handles both cases.
 *
 * IMPORTANT: initialize() must be called once at app startup before logging any crashes.
 * It resolves the Firebase Crashlytics instance and validates configuration.
 *
 * Thread-safe: crashlytics and isInitialised are volatile to prevent race conditions
 * in concurrent environments.
 */
class AndroidCrashLogger : CrashLogger {

    /**
     * Initialised by initialize(); null when Firebase is not configured.
     * Volatile to ensure visibility across threads.
     */
    @Volatile
    private var crashlytics: FirebaseCrashlytics? = null

    @Volatile
    private var isInitialised = false

    override fun initialize() {
        // Guard against multiple initializations
        if (isInitialised) {
            Log.w(TAG, "initialize() called multiple times — ignoring")
            return
        }

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

        isInitialised = true
    }

    override fun logException(throwable: Throwable, message: String?) {
        ensureInitialised()
        
        // Log message to Crashlytics for context (message is diagnostic, not PII).
        if (message != null) {
            crashlytics?.log(message)
        }
        crashlytics?.recordException(throwable)
        
        // Also log to Android Log for local debugging.
        Log.e(TAG, message ?: "Exception occurred", throwable)
    }

    override fun setCustomKey(key: String, value: String) {
        ensureInitialised()
        
        // Truncate key/value to Firebase limits per documentation.
        val safeKey = if (key.length > MAX_KEY_LENGTH) {
            Log.w(TAG, "Custom key too long (${key.length} > $MAX_KEY_LENGTH), truncating")
            key.substring(0, MAX_KEY_LENGTH)
        } else {
            key
        }

        val safeValue = if (value.length > MAX_VALUE_LENGTH) {
            Log.w(TAG, "Custom value too long (${value.length} > $MAX_VALUE_LENGTH), truncating")
            value.substring(0, MAX_VALUE_LENGTH)
        } else {
            value
        }
        
        // Set in Crashlytics (safe for non-sensitive values).
        crashlytics?.setCustomKey(safeKey, safeValue)
    }

    override fun setUserId(userId: String) {
        ensureInitialised()
        
        // Do NOT log userId to logcat — it is sensitive/PII.
        // Only set in Crashlytics if available.
        crashlytics?.setUserId(userId)
    }

    override fun log(message: String) {
        ensureInitialised()
        
        // Truncate message to prevent log buffer overflow.
        val safeMessage = if (message.length > MAX_VALUE_LENGTH) {
            message.substring(0, MAX_VALUE_LENGTH) + "… (truncated)"
        } else {
            message
        }
        
        // Log to both Crashlytics and Android Log.
        crashlytics?.log(safeMessage)
        Log.d(TAG, safeMessage)
    }

    /**
     * Force a crash for testing purposes (debug builds only).
     * Release builds log a warning and return gracefully to prevent production crashes.
     * This allows QA to test Crashlytics in debug/staging without risking production stability.
     */
    override fun testCrash() {
        ensureInitialised()
        
        if (BuildConfig.DEBUG) {
            throw IllegalStateException("Test crash for Firebase Crashlytics")
        } else {
            Log.w(TAG, "testCrash() is disabled in release builds to prevent accidental crashes")
        }
    }

    /**
     * Verify initialize() was called before any logging operation.
     * Logs a warning if not — the operation will proceed best-effort, but crashes may be lost.
     */
    private fun ensureInitialised() {
        if (!isInitialised) {
            Log.e(TAG, "CrashLogger.initialize() was not called at startup — crashes may not be reported. " +
                    "Call initialize() early in your Application.onCreate() or Compose App initialization.")
        }
    }
}

/**
 * Create Android-specific crash logger.
 * Caller MUST call initialize() before logging any crashes.
 */
actual fun createCrashLogger(): CrashLogger = AndroidCrashLogger()
