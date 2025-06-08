package com.example.myproject.di

import org.koin.dsl.module

val appModule = module {
    includes(crashLoggingModule)
}
