package io.asterixorobelix.afrikaburn

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.asterixorobelix.afrikaburn.di.appModule
import io.asterixorobelix.afrikaburn.platform.CrashLogger
import io.asterixorobelix.afrikaburn.platform.FirebaseConfigChecker
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject

import afrikaburn.composeapp.generated.resources.Res
import afrikaburn.composeapp.generated.resources.compose_multiplatform
import org.koin.compose.KoinApplication

@Composable
@Preview
fun App() {
    var showContent by remember { mutableStateOf(false) }
    val isDarkTheme = isSystemInDarkTheme()

    KoinApplication(application = {
        modules(appModule)
    }) {
        // Initialize crash logging
        val crashLogger: CrashLogger = koinInject()
        LaunchedEffect(Unit) {
            crashLogger.initialize()
            crashLogger.log("App started successfully")
            
            // Check Firebase configuration status
            FirebaseConfigChecker.logConfigurationStatus(crashLogger)
        }
        
        AppTheme(useDarkTheme = isDarkTheme) {
            Column(
                modifier = Modifier
                    .safeContentPadding()
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(onClick = { showContent = !showContent }) {
                    Text(
                        "Click me!",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                AnimatedVisibility(showContent) {
                    val greeting = remember { Greeting().greet() }
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(painterResource(Res.drawable.compose_multiplatform), null)
                        Text(
                            "Compose: $greeting",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }

}