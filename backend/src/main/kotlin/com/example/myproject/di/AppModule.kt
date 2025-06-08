package com.example.myproject.di

import org.koin.dsl.module

val appModule = module {
    // Database
    // single { DatabaseConfig() }
    
    // Repositories
    // single<CustomerRepository> { CustomerRepositoryImpl() }
    
    // Services
    // single { CustomerService(get()) }
    
    // HTTP Client
    // single {
    //     HttpClient(CIO) {
    //         install(ContentNegotiation) {
    //             json()
    //         }
    //     }
    // }
}