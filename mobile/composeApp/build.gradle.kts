import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.detekt)
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.firebaseCrashlytics) apply false
    alias(libs.plugins.sqldelight)
}

kotlin {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
            
            // Optimize for CI builds
            if (System.getenv("CI") == "true") {
                freeCompilerArgs += listOf(
                    "-Xruntime-logs=gc=info",
                    "-Xallocator=custom"
                )
            }
        }
    }
    
    // Create XCFramework for iOS (ARM64 targets only)
    tasks.register("assembleDebugXCFramework") {
        dependsOn("linkDebugFrameworkIosArm64")
        dependsOn("linkDebugFrameworkIosSimulatorArm64")
        group = "multiplatform"
        description = "Assembles debug XCFramework for ARM64 iOS targets"
    }

    tasks.register("assembleReleaseXCFramework") {
        dependsOn("linkReleaseFrameworkIosArm64")
        dependsOn("linkReleaseFrameworkIosSimulatorArm64")
        group = "multiplatform"
        description = "Assembles release XCFramework for all iOS targets"
    }

    tasks.register("assembleReleaseFrameworkDevice") {
        dependsOn("linkReleaseFrameworkIosArm64")
        group = "multiplatform"
        description = "Assembles release framework for device (arm64 only, used by CD)"
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.material)
            implementation(libs.play.services.location)
            implementation(libs.sqldelight.android.driver)

            // Firebase for Android - moved to conditional dependencies section
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.androidx.navigation.compose)

            implementation(libs.material.icons.extended)

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor)

            implementation(libs.maplibre.compose)
            implementation(libs.maplibre.compose.material3)

            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        
        androidUnitTest.dependencies {
            implementation(libs.mockk)
        }

        androidInstrumentedTest.dependencies {
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.test.runner)
            implementation(libs.androidx.test.rules)
            implementation(libs.androidx.espresso.core)
            implementation(libs.androidx.uiautomator)
            implementation(libs.androidx.compose.ui.test.junit4)
            implementation(libs.fastlane.screengrab)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }
    }
}

// Apply Google Services plugin conditionally
if (file("google-services.json").exists() || file("src/google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
    apply(plugin = "com.google.firebase.crashlytics")
}

android {
    namespace = "io.asterixorobelix.afrikaburn"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "io.asterixorobelix.afrikaburn"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 44
        versionName = "2.1.41"
        testInstrumentationRunner = "io.asterixorobelix.afrikaburn.TestRunner"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    
    // Add Firebase dependencies only if Google Services is available
    if (file("google-services.json").exists() || file("src/google-services.json").exists()) {
        implementation(project.dependencies.platform(libs.firebase.bom))
        implementation(libs.firebase.crashlytics)
        implementation(libs.firebase.analytics)
    }
}

// Configure detekt for this subproject
detekt {
    toolVersion = rootProject.libs.versions.detekt.get()
    config.from(rootProject.file("detekt-mobile.yml"))
    buildUponDefaultConfig = false
    allRules = false
    source.from("src/commonMain/kotlin", "src/androidMain/kotlin", "src/iosMain/kotlin")
}

// Configure detekt task reports and JVM target
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "11"
    reports {
        xml.required.set(true)
        html.required.set(true)
        txt.required.set(false)
        sarif.required.set(false)
        md.required.set(false)
    }
}

// SQLDelight database configuration
sqldelight {
    databases {
        create("AfrikaBurnDatabase") {
            packageName.set("io.asterixorobelix.afrikaburn.data.database")
        }
    }
}
