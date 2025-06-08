plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.detekt)
}

// Configure Detekt for root project
detekt {
    toolVersion = libs.versions.detekt.get()
    config.from(file("detekt-mobile.yml"))
    buildUponDefaultConfig = false
    allRules = false
}

// Configure detekt reports for all projects
allprojects {
    tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
        reports {
            xml.required.set(true)
            html.required.set(true)
            txt.required.set(false)
            sarif.required.set(false)
            md.required.set(false)
        }
    }
}

// Configure test reporting for all subprojects
subprojects {
    tasks.withType<Test>().configureEach {
        // Generate test reports for CI
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
        
        // Better test output
        testLogging {
            events("passed", "failed", "skipped")
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
            showStandardStreams = false
        }
    }
}