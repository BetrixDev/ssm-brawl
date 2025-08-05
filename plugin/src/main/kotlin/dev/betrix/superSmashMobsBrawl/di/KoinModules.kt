package dev.betrix.superSmashMobsBrawl.di

import dev.betrix.superSmashMobsBrawl.services.DataService
import dev.betrix.superSmashMobsBrawl.services.QueueService
import dev.betrix.superSmashMobsBrawl.services.KitService
import dev.betrix.superSmashMobsBrawl.services.MinigameService
import dev.betrix.superSmashMobsBrawl.services.HotbarService
import org.koin.dsl.module

val appModule = module {
    // Provide services as singletons
    single { DataService }
    single { QueueService }
    single { KitService }
    single { MinigameService }
    single { HotbarService }
}