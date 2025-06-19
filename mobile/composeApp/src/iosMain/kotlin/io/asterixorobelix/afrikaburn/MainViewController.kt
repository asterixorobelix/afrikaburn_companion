package io.asterixorobelix.afrikaburn

import androidx.compose.ui.window.ComposeUIViewController
import org.koin.core.error.KoinAppAlreadyStartedException

@Suppress("FunctionNaming")
fun MainViewController() = ComposeUIViewController { 
    // Initialize Koin if not already done
    try {
        KoinInitializer.init()
    } catch (e: KoinAppAlreadyStartedException) {
        // Koin already initialized, continue
    }
    App() 
}
