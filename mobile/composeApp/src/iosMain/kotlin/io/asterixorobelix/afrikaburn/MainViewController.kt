package io.asterixorobelix.afrikaburn

import androidx.compose.ui.window.ComposeUIViewController

private var isKoinInitialized = false

@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { 
    // Initialize Koin if not already done
    if (!isKoinInitialized) {
        try {
            KoinInitializer.init()
            isKoinInitialized = true
        } catch (e: IllegalStateException) {
            // Koin is already initialized, which is expected
            println("Koin already initialized: ${e.message}")
            isKoinInitialized = true
        }
    }
    App() 
}
