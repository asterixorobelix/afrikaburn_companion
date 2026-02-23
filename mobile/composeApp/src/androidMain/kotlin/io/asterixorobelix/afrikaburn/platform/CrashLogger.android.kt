package io.asterixorobelix.afrikaburn.platform

import android.util.Log

/**
 * Android implementation that can work with or without Firebase Crashlytics
 */
class AndroidCrashLogger : CrashLogger {
    private var crashlytics: Any? = null
    private val tag = "CrashLogger"
    
    init {
        // Try to load Firebase Crashlytics if available
        try {
            val crashlyticsClass = Class.forName("com.google.firebase.crashlytics.FirebaseCrashlytics")
            val getInstanceMethod = crashlyticsClass.getMethod("getInstance")
            crashlytics = getInstanceMethod.invoke(null)
        } catch (@Suppress("SwallowedException") e: ClassNotFoundException) {
            Log.w(tag, "Firebase Crashlytics not available, using fallback logging")
        } catch (e: NoSuchMethodException) {
            Log.w(tag, "Firebase Crashlytics method not found, using fallback logging", e)
        } catch (e: IllegalAccessException) {
            Log.w(tag, "Cannot access Firebase Crashlytics, using fallback logging", e)
        } catch (e: java.lang.reflect.InvocationTargetException) {
            Log.w(tag, "Firebase Crashlytics initialization failed, using fallback logging", e)
        }
    }
    
    override fun initialize() {
        if (crashlytics != null) {
            try {
                val setCrashlyticsCollectionEnabledMethod = crashlytics!!.javaClass
                    .getMethod("setCrashlyticsCollectionEnabled", Boolean::class.java)
                setCrashlyticsCollectionEnabledMethod.invoke(crashlytics, true)
                
                // Log warning if using default template
                if (FirebaseConfigChecker.IS_USING_DEFAULT_TEMPLATE) {
                    log("⚠️ WARNING: Using default Firebase template - Crashlytics disabled")
                    log("Replace google-services.json with your Firebase project configuration")
                }
            } catch (e: NoSuchMethodException) {
                Log.e(tag, "Error initializing Crashlytics", e)
            } catch (e: IllegalAccessException) {
                Log.e(tag, "Error initializing Crashlytics", e)
            }
        } else {
            Log.i(tag, "Crashlytics not available - using Android logging")
        }
    }
    
    override fun logException(throwable: Throwable, message: String?) {
        if (crashlytics != null) {
            try {
                if (message != null) {
                    val logMethod = crashlytics!!.javaClass.getMethod("log", String::class.java)
                    logMethod.invoke(crashlytics, message)
                }
                val recordExceptionMethod = crashlytics!!.javaClass
                    .getMethod("recordException", Throwable::class.java)
                recordExceptionMethod.invoke(crashlytics, throwable)
            } catch (e: NoSuchMethodException) {
                Log.e(tag, "Error logging exception to Crashlytics", e)
                fallbackLogException(throwable, message)
            } catch (e: IllegalAccessException) {
                Log.e(tag, "Error logging exception to Crashlytics", e)
                fallbackLogException(throwable, message)
            }
        } else {
            fallbackLogException(throwable, message)
        }
    }
    
    override fun setCustomKey(key: String, value: String) {
        if (crashlytics != null) {
            try {
                val setCustomKeyMethod = crashlytics!!.javaClass
                    .getMethod("setCustomKey", String::class.java, String::class.java)
                setCustomKeyMethod.invoke(crashlytics, key, value)
            } catch (e: NoSuchMethodException) {
                Log.e(tag, "Error setting custom key", e)
                Log.d(tag, "Custom key: $key = $value")
            } catch (e: IllegalAccessException) {
                Log.e(tag, "Error setting custom key", e)
                Log.d(tag, "Custom key: $key = $value")
            }
        } else {
            Log.d(tag, "Custom key: $key = $value")
        }
    }
    
    override fun setUserId(userId: String) {
        if (crashlytics != null) {
            try {
                val setUserIdMethod = crashlytics!!.javaClass
                    .getMethod("setUserId", String::class.java)
                setUserIdMethod.invoke(crashlytics, userId)
            } catch (e: NoSuchMethodException) {
                Log.e(tag, "Error setting user ID", e)
                Log.d(tag, "User ID: $userId")
            } catch (e: IllegalAccessException) {
                Log.e(tag, "Error setting user ID", e)
                Log.d(tag, "User ID: $userId")
            }
        } else {
            Log.d(tag, "User ID: $userId")
        }
    }
    
    override fun log(message: String) {
        if (crashlytics != null) {
            try {
                val logMethod = crashlytics!!.javaClass.getMethod("log", String::class.java)
                logMethod.invoke(crashlytics, message)
            } catch (e: NoSuchMethodException) {
                Log.e(tag, "Error logging message", e)
                Log.d(tag, message)
            } catch (e: IllegalAccessException) {
                Log.e(tag, "Error logging message", e)
                Log.d(tag, message)
            }
        } else {
            Log.d(tag, message)
        }
    }

    @Suppress("UseCheckOrError")
    override fun testCrash() {
        throw IllegalStateException("Test crash for Firebase Crashlytics")
    }
    
    private fun fallbackLogException(throwable: Throwable, message: String?) {
        message?.let {
            Log.e(tag, it, throwable)
        } ?: Log.e(tag, "Exception occurred", throwable)
    }
}

/**
 * Create Android-specific crash logger
 */
actual fun createCrashLogger(): CrashLogger = AndroidCrashLogger()