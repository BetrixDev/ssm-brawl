package dev.betrix.superSmashMobsBrawl.di

import dev.betrix.superSmashMobsBrawl.services.DataService
import org.koin.dsl.module

val appModule = module {
    // Provide DataService as a singleton
    single { DataService }
    
    // You can add other services here as needed
    // For example:
    // single { HotbarService }
    // single { KitService }
}