package com.example.myproject.di

import com.example.myproject.platform.CrashLogger
import com.example.myproject.platform.createCrashLogger
import org.koin.dsl.module

/**
 * Koin module for crash logging dependencies
 */
val crashLoggingModule = module {
    single<CrashLogger> { createCrashLogger() }
}